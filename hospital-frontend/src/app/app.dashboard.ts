import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from './services/api.service';
import { SupabaseService } from './services/supabase.service';
import { Chart } from 'chart.js/auto';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class DashboardComponent implements OnInit {
  // Auth
  currentUser: any = null;
  userRole: string = '';

  selectedHospitalId: number = 1;

  hospitals: any[] = [];
  selectedHospital: any = null;
  occupancyChart: Chart | null = null;
  departments: any[] = [];
  doctors: any[] = [];
  wards: any[] = [];
  beds: any[] = [];
  queue: any[] = [];
  transfers: any[] = [];
  admissions: any[] = [];

  // Patient Booking Form
  patientView: string = 'DASHBOARD';
  patientTokens: any[] = [];
  patientScheduledAppointments: any[] = [];
  patientHistoryRecords: any[] = [];
  bookingForMode: string = 'MYSELF';
  bookingForm = {
    patientName: '',
    patientPhone: '',
    departmentId: null as number | null,
    doctorId: null as number | null,
    priority: 'NORMAL',
    bookingMode: 'WALK_IN', // 'WALK_IN' or 'SCHEDULED'
    slotStart: null as string | null
  };
  availableSlots: any[] = [];
  slotsLoading: boolean = false;
  
  // Calendar State
  currentDate = new Date();
  calendarMonth: number = this.currentDate.getMonth() + 1;
  calendarYear: number = this.currentDate.getFullYear();
  calendarDays: { date: Date, dateStr: string, hasAvailability: boolean, isPast: boolean }[] = [];
  selectedDateStr: string | null = null;
  calendarLoading: boolean = false;
  
  // Doctor Availability Management
  showAvailabilityModal: boolean = false;
  availabilityLoading: boolean = false;
  availabilityForm: any[] = [];
  daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  // Bed Search Form
  bedSearchType: string = 'GENERAL';
  bedSearchResults: any[] = [];
  bedSearchLoading: boolean = false;
  latestToken: any = null;
  isLoading: boolean = true;

  // Doctor Workstation
  currentDoctorId: number = 1;
  selectedDoctor: any = null;

  // Department Drill-down
  selectedDepartmentForView: any = null;
  departmentDoctorsForView: any[] = [];

  // Transfer Modal
  showTransferModal: boolean = false;
  transferLoading: boolean = false;
  transferBedType: string = 'ICU';

  showAdmitModal: boolean = false;
  admitTargetItem: any = null;
  admitBedType: string = 'ICU';
  bedTypeOptions: string[] = ['GENERAL', 'ICU', 'EMERGENCY', 'HDU', 'PEDIATRIC', 'MATERNITY', 'ISOLATION'];
  transferTargetPatient: any = null;
  recommendations: any[] = [];
  selectedDestinationHospital: any = null;

  // What-If Simulator Modal
  showWhatIfModal: boolean = false;
  whatIfInput = { additionalIcuPatients: 30 };
  whatIfResult: any = null;

  // Alert Banners
  notificationAlert: string | null = null;

  // Patient History Modal (doctor view)
  showHistoryModal: boolean = false;
  historyPatientName: string = '';
  historyRecords: any[] = [];
  historyLoading: boolean = false;

  // Complete Consultation -> record diagnosis/prescription (doctor view)
  showRecordModal: boolean = false;
  recordTargetItem: any = null;
  recordForm = {
    symptoms: '',
    diagnosis: '',
    prescription: '',
    notes: '',
    bloodPressure: '',
    temperatureCelsius: null as number | null,
    pulseRate: null as number | null,
    weightKg: null as number | null,
    followUpDate: ''
  };
  recordError: string | null = null;
  recordSaving: boolean = false;

  // Add Doctor Modal
  showAddDoctorModal: boolean = false;
  addDoctorLoading: boolean = false;
  addDoctorError: string | null = null;
  addDoctorSuccessMessage: string | null = null;
  newDoctorForm = {
    name: '',
    email: '',
    phone: '',
    departmentId: null as number | null
  };

  constructor(private apiService: ApiService, private cdr: ChangeDetectorRef, private router: Router, private supabaseService: SupabaseService) {}

  ngOnInit(): void {
    // Read user from localStorage
    const userJson = localStorage.getItem('auth_user');
    if (!userJson) {
      this.router.navigate(['/login']);
      return;
    }

    this.currentUser = JSON.parse(userJson);
    this.userRole = this.currentUser.role;

    // Pre-fill patient booking form with logged-in patient's info
    if (this.userRole === 'PATIENT') {
      this.bookingForm.patientName = this.currentUser.name;
      this.bookingForm.patientPhone = this.currentUser.phone || '';
      this.loadPatientData();
    }

    // If user is tied to a hospital, lock to that hospital
    if (this.currentUser.hospitalId) {
      this.selectedHospitalId = this.currentUser.hospitalId;
    }

    // Doctors only see their own queue, not every department's patients.
    if (this.userRole === 'DOCTOR' && this.currentUser.doctorId) {
      this.currentDoctorId = this.currentUser.doctorId;
    }

    this.loadHospitals();
    this.loadTransfers();
  }

  async logout(): Promise<void> {
    await this.supabaseService.signOut();
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    this.router.navigate(['/login']);
  }

  loadHospitals(): void {
    this.apiService.getHospitals().subscribe({
      next: (data) => {
        this.hospitals = data;
        if (this.hospitals.length > 0) {
          this.onHospitalSelect(this.selectedHospitalId);
        }
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load hospitals', err);
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadPatientData(): void {
    if (this.currentUser && this.currentUser.phone && this.currentUser.name) {
      this.apiService.getPatientQueueByPhoneAndName(this.currentUser.phone, this.currentUser.name).subscribe(data => {
        this.patientTokens = data.sort((a, b) => b.id - a.id);
        this.cdr.markForCheck();
      });
      this.apiService.getPatientAppointmentsByPhoneAndName(this.currentUser.phone, this.currentUser.name).subscribe(data => {
        this.patientScheduledAppointments = data.sort((a, b) => new Date(a.appointmentTime).getTime() - new Date(b.appointmentTime).getTime());
        // 2. Fetch Medical History (Consultations)
        if (this.currentUser.phone && this.currentUser.name) {
          this.apiService.getPatientHistoryByPhoneAndName(this.currentUser.phone, this.currentUser.name).subscribe(data => {
            this.patientHistoryRecords = data.sort((a, b) => new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime());
          });
        }
        this.cdr.markForCheck();
      });
    }
  }

  loadHospitalData(): void {
    if (!this.selectedHospitalId) return;
    this.isLoading = true;

    this.apiService.getHospitalById(this.selectedHospitalId).subscribe(data => {
      this.selectedHospital = data;
      this.cdr.markForCheck();
    });

    this.apiService.getHospitalQueue(this.selectedHospitalId).subscribe(data => {
      this.queue = data;
      this.cdr.markForCheck();
    });

    this.apiService.getWards(this.selectedHospitalId).subscribe(data => {
      this.wards = data;
      this.cdr.markForCheck();
    });

    this.apiService.getTransfersForHospital(this.selectedHospitalId).subscribe(data => {
      this.transfers = data;
      this.cdr.markForCheck();
    });

    this.apiService.getAdmissions(this.selectedHospitalId).subscribe(data => {
      this.admissions = data;
      this.isLoading = false;
      this.cdr.markForCheck();
      this.renderChart();
    });
  }

  onHospitalSelect(hospitalId: number): void {
    this.selectedHospitalId = Number(hospitalId);
    this.apiService.getHospitalById(this.selectedHospitalId).subscribe(h => {
      this.selectedHospital = h;
      this.cdr.markForCheck();
      this.renderChart();
    });
    this.loadDepartments(this.selectedHospitalId);
    this.loadDoctors(this.selectedHospitalId);
    this.loadBeds(this.selectedHospitalId);
    this.refreshQueue();
    this.loadAdmissions(this.selectedHospitalId);
  }

  renderChart(): void {
    if (!this.selectedHospital || this.userRole !== 'HOSPITAL_ADMIN') return;
    setTimeout(() => {
      const canvas = document.getElementById('occupancyChart') as HTMLCanvasElement;
      if (!canvas) return;
      
      if (this.occupancyChart) {
        this.occupancyChart.destroy();
      }

      const totalOccupied = (this.selectedHospital.totalBeds || 0) - (this.selectedHospital.availableBeds || 0);
      const available = this.selectedHospital.availableBeds || 0;

      this.occupancyChart = new Chart(canvas, {
        type: 'doughnut',
        data: {
          labels: ['Occupied', 'Available'],
          datasets: [{
            data: [totalOccupied, available],
            backgroundColor: ['#f43f5e', '#10b981'],
            borderWidth: 0
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '70%',
          plugins: {
            legend: { position: 'bottom' }
          }
        }
      });
    }, 150);
  }

  loadDepartments(hospitalId: number): void {
    this.apiService.getDepartments(hospitalId).subscribe(data => {
      this.departments = data;
      if (data.length > 0) this.bookingForm.departmentId = data[0].id;
      this.cdr.markForCheck();
    });
  }

  loadDoctors(hospitalId: number): void {
    this.apiService.getDoctors(hospitalId).subscribe(data => {
      this.doctors = data;
      if (data.length > 0) {
        this.bookingForm.doctorId = data[0].id;
        this.selectedDoctor = data[0];
      }
      this.cdr.markForCheck();
    });
  }

  loadBeds(hospitalId: number): void {
    this.apiService.getBeds(hospitalId).subscribe(data => {
      this.beds = data;
      this.cdr.markForCheck();
    });
  }

  loadQueue(hospitalId: number): void {
    this.apiService.getHospitalQueue(hospitalId).subscribe(data => {
      this.queue = data;
      this.cdr.markForCheck();
    });
  }

  loadDoctorQueue(doctorId: number): void {
    this.apiService.getDoctorQueue(doctorId).subscribe(data => {
      this.queue = data;
      this.cdr.markForCheck();
    });
  }

  // Doctors should only ever see their own patients on the dashboard, not
  // every patient across every department in the hospital. Hospital admins /
  // city admins still see the full hospital-wide queue.
  refreshQueue(): void {
    if (this.userRole === 'DOCTOR' && this.currentDoctorId) {
      this.loadDoctorQueue(this.currentDoctorId);
    } else {
      this.loadQueue(this.selectedHospitalId);
    }
  }

  loadAdmissions(hospitalId: number): void {
    this.apiService.getAdmissions(hospitalId).subscribe(data => {
      this.admissions = data;
      this.cdr.markForCheck();
    });
  }

  searchBeds(): void {
    this.bedSearchLoading = true;
    this.apiService.getHospitalRecommendations(null, this.bedSearchType).subscribe({
      next: (data) => {
        this.bedSearchResults = data;
        this.bedSearchLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.bedSearchLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadTransfers(): void {
    this.apiService.getAllTransfers().subscribe(data => {
      this.transfers = data;
      this.cdr.markForCheck();
    });
  }

  onDepartmentChange(): void {
    if (this.bookingForm.departmentId) {
      this.apiService.getDoctors(this.selectedHospitalId, this.bookingForm.departmentId ?? undefined).subscribe(docs => {
        this.doctors = docs;
        if (docs.length > 0) this.bookingForm.doctorId = docs[0].id;
        this.cdr.markForCheck();
      });
    }
  }

  viewDepartmentDoctors(dept: any): void {
    this.selectedDepartmentForView = dept;
    this.departmentDoctorsForView = [];
    this.apiService.getDoctors(this.selectedHospitalId, dept.id).subscribe(docs => {
      this.departmentDoctorsForView = docs;
      this.cdr.markForCheck();
    });
  }

  bookFromDepartment(doc: any): void {
    if (this.selectedDepartmentForView) {
      this.bookingForm.departmentId = this.selectedDepartmentForView.id;
      this.patientView = 'APPOINTMENT';
      this.apiService.getDoctors(this.selectedHospitalId, this.selectedDepartmentForView.id).subscribe(docs => {
        this.doctors = docs;
        this.bookingForm.doctorId = doc.id;
        this.cdr.markForCheck();
      });
    }
  }

  onBookingModeChange(): void {
    if (this.bookingForMode === 'MYSELF') {
      this.bookingForm.patientName = this.currentUser.name;
    } else {
      this.bookingForm.patientName = '';
    }
  }

  formatDoctorName(name: string): string {
    if (!name) return '';
    let cleanName = name.replace(/^(Dr\.\s*)+/i, '');
    return 'Dr. ' + cleanName;
  }

  submitBooking(): void {
    if (!this.bookingForm.doctorId || !this.bookingForm.departmentId) return;

    const payload: any = {
      patientId: this.currentUser.id,
      patientName: this.bookingForm.patientName,
      patientPhone: this.bookingForm.patientPhone,
      hospitalId: this.selectedHospitalId,
      departmentId: this.bookingForm.departmentId,
      doctorId: this.bookingForm.doctorId,
      priority: this.bookingForm.priority,
      bookingChannel: 'WEB'
    };

    if (this.bookingForm.bookingMode === 'SCHEDULED') {
      if (!this.bookingForm.slotStart) {
        this.showToast('Please select an available slot.');
        return;
      }
      payload.slotStart = this.bookingForm.slotStart;
      this.apiService.scheduleAppointment(payload).subscribe({
        next: (appt) => {
          this.showToast(`Appointment scheduled for ${new Date(appt.appointmentTime).toLocaleString()}`);
          if (this.userRole === 'PATIENT') {
            this.loadPatientData();
          }
          this.patientView = 'DASHBOARD';
          this.cdr.markForCheck();
        },
        error: (err) => this.showToast('Scheduling failed: ' + (err.error?.message || err.message))
      });
    } else {
      this.apiService.bookAppointment(payload).subscribe({
        next: (token) => {
          this.latestToken = token;
          this.showToast(`Token ${token.tokenNumber} issued — estimated wait: ${token.estimatedWaitMinutes} min`);
          this.loadQueue(this.selectedHospitalId);
          if (this.userRole === 'PATIENT') {
            this.loadPatientData();
          }
          this.cdr.markForCheck();
        },
        error: (err) => this.showToast('Booking failed: ' + (err.error?.message || err.message))
      });
    }
  }

  generateCalendar(): void {
    if (!this.bookingForm.doctorId) return;
    this.calendarLoading = true;
    this.apiService.getDoctorAvailabilitySummary(this.bookingForm.doctorId, this.calendarMonth, this.calendarYear).subscribe({
      next: (summary) => {
        const firstDay = new Date(this.calendarYear, this.calendarMonth - 1, 1).getDay(); // 0-6
        const daysInMonth = new Date(this.calendarYear, this.calendarMonth, 0).getDate();
        
        this.calendarDays = [];
        
        // Padding start
        for (let i = 0; i < firstDay; i++) {
          this.calendarDays.push(null as any);
        }
        
        // Days
        for (let i = 1; i <= daysInMonth; i++) {
          const dateStr = `${this.calendarYear}-${String(this.calendarMonth).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
          const s = summary.find((x: any) => x.date === dateStr);
          this.calendarDays.push({
            date: new Date(this.calendarYear, this.calendarMonth - 1, i),
            dateStr: dateStr,
            hasAvailability: s ? s.hasAvailability : false,
            isPast: new Date(dateStr) < new Date(new Date().toISOString().split('T')[0])
          });
        }
        
        this.calendarLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.calendarLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  changeMonth(offset: number): void {
    let d = new Date(this.calendarYear, this.calendarMonth - 1 + offset, 1);
    this.calendarMonth = d.getMonth() + 1;
    this.calendarYear = d.getFullYear();
    this.selectedDateStr = null;
    this.availableSlots = [];
    this.bookingForm.slotStart = null;
    this.generateCalendar();
  }

  getCalendarMonthName(): string {
    const d = new Date(this.calendarYear, this.calendarMonth - 1, 1);
    return d.toLocaleString('default', { month: 'long', year: 'numeric' });
  }

  selectDate(day: any): void {
    if (!day || day.isPast || !day.hasAvailability) return;
    this.selectedDateStr = day.dateStr;
    this.bookingForm.slotStart = null;
    this.fetchSlotsForDate(day.dateStr);
  }

  fetchSlotsForDate(dateStr: string): void {
    if (!this.bookingForm.doctorId) return;
    this.slotsLoading = true;
    this.apiService.getAvailableSlots(this.bookingForm.doctorId, dateStr, dateStr).subscribe({
      next: (slots) => {
        this.availableSlots = slots.filter(s => s.available);
        this.slotsLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.slotsLoading = false;
        this.showToast('Failed to load slots');
      }
    });
  }

  checkInScheduled(appointmentId: number): void {
    this.apiService.checkInScheduledAppointment(appointmentId).subscribe({
      next: (token) => {
        this.showToast(`Checked in! Token ${token.tokenNumber} issued.`);
        this.loadPatientData();
      },
      error: (err) => this.showToast('Check-in failed: ' + (err.error?.message || err.message))
    });
  }

  openAvailabilityModal(): void {
    if (this.userRole !== 'DOCTOR' && this.userRole !== 'HOSPITAL_ADMIN') return;
    const docId = this.userRole === 'DOCTOR' ? this.currentDoctorId : this.selectedDoctor?.id;
    if (!docId) return;
    
    this.availabilityLoading = true;
    this.showAvailabilityModal = true;
    this.apiService.getDoctorAvailability(docId).subscribe(data => {
      this.availabilityForm = data;
      this.availabilityLoading = false;
      this.cdr.markForCheck();
    });
  }

  addAvailabilityRow(): void {
    this.availabilityForm.push({
      dayOfWeek: 'MONDAY',
      startTime: '09:00:00',
      endTime: '17:00:00',
      slotDurationMinutes: 15
    });
  }

  removeAvailabilityRow(index: number): void {
    this.availabilityForm.splice(index, 1);
  }

  saveAvailability(): void {
    const docId = this.userRole === 'DOCTOR' ? this.currentDoctorId : this.selectedDoctor?.id;
    if (!docId) return;
    
    this.availabilityLoading = true;
    // ensure seconds are attached to HH:mm for java.time.LocalTime
    const payload = this.availabilityForm.map(row => ({
      ...row,
      startTime: row.startTime.length === 5 ? row.startTime + ':00' : row.startTime,
      endTime: row.endTime.length === 5 ? row.endTime + ':00' : row.endTime
    }));
    
    this.apiService.saveDoctorAvailability(docId, payload).subscribe({
      next: () => {
        this.showToast('Availability saved successfully.');
        this.showAvailabilityModal = false;
        this.availabilityLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.showToast('Failed to save availability');
        this.availabilityLoading = false;
      }
    });
  }

  updateQueueStatus(entryId: number, newStatus: string): void {
    this.apiService.updateQueueStatus(entryId, newStatus).subscribe(() => {
      this.refreshQueue();
      this.showToast(`Queue entry updated to ${newStatus}`);
    });
  }

  // ── Patient History (doctor view) ───────────────────────────────────────
  openPatientHistory(item: any): void {
    this.historyPatientName = item.patientName;
    this.historyRecords = [];
    this.historyLoading = true;
    this.showHistoryModal = true;

    const lookup = (item.patientPhone && item.patientName)
      ? this.apiService.getPatientHistoryByPhoneAndName(item.patientPhone, item.patientName)
      : this.apiService.getPatientHistory(item.patientId);

    lookup.subscribe({
      next: (records) => {
        this.historyRecords = records;
        this.historyLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.historyLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  closeHistoryModal(): void {
    this.showHistoryModal = false;
  }

  // ── Complete Consultation -> save diagnosis/prescription ───────────────
  openCompleteConsultation(item: any): void {
    this.recordTargetItem = item;
    this.recordForm = {
      symptoms: '',
      diagnosis: '',
      prescription: '',
      notes: '',
      bloodPressure: '',
      temperatureCelsius: null,
      pulseRate: null,
      weightKg: null,
      followUpDate: ''
    };
    this.showRecordModal = true;
  }

  closeRecordModal(): void {
    this.showRecordModal = false;
    this.recordTargetItem = null;
  }

  submitConsultationRecord(): void {
    if (!this.recordTargetItem || this.recordSaving) return;
    const item = this.recordTargetItem;
    this.recordError = null;
    this.recordSaving = true;

    const payload = {
      patientId: item.patientId ?? null,
      patientName: item.patientName,
      patientPhone: item.patientPhone,
      hospitalId: this.selectedHospitalId,
      doctorId: this.currentDoctorId,
      doctorName: this.currentUser?.name,
      appointmentId: item.appointmentId,
      symptoms: this.recordForm.symptoms,
      diagnosis: this.recordForm.diagnosis,
      prescription: this.recordForm.prescription,
      notes: this.recordForm.notes,
      bloodPressure: this.recordForm.bloodPressure,
      temperatureCelsius: this.recordForm.temperatureCelsius,
      pulseRate: this.recordForm.pulseRate,
      weightKg: this.recordForm.weightKg,
      followUpDate: this.recordForm.followUpDate || null
    };

    this.apiService.createMedicalRecord(payload).subscribe({
      next: () => {
        // Only mark the queue entry COMPLETED once the visit record is saved,
        // so a doctor can never lose a consultation's history by mis-clicking.
        this.apiService.updateQueueStatus(item.id, 'COMPLETED').subscribe(() => {
          this.showToast(`Consultation completed for ${item.patientName}`);
          this.recordSaving = false;
          this.closeRecordModal();
          this.refreshQueue();
          this.cdr.markForCheck();
        });
      },
      error: (err) => {
        this.recordSaving = false;
        if (err.status === 400) {
          this.recordError = typeof err.error === 'string' ? err.error : (err.error?.message || 'Invalid input.');
        } else {
          this.recordError = 'Could not save visit record. Please try again.';
        }
        this.cdr.markForCheck();
      }
    });
  }

  triggerTransferModal(item: any, bedType: string = 'ICU'): void {
    this.transferTargetPatient = { patientId: item.patientId ?? null, name: item.patientName };
    this.transferBedType = bedType;
    this.showTransferModal = true;
    this.transferLoading = true;
    this.recommendations = [];
    this.selectedDestinationHospital = null;

    this.apiService.getHospitalRecommendations(this.selectedHospitalId, bedType).subscribe({
      next: (recs) => {
        this.recommendations = recs;
        if (recs.length > 0) this.selectedDestinationHospital = recs[0];
        this.transferLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.transferLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  confirmTransferInitiation(): void {
    if (!this.selectedDestinationHospital) return;

    this.apiService.initiateTransfer(
      this.transferTargetPatient.patientId,
      this.transferTargetPatient.name,
      this.selectedHospitalId,
      this.selectedDestinationHospital.hospital.id,
      this.transferBedType,
      'CRITICAL',
      `Source facility ${this.transferBedType} at capacity. System-generated recommendation match.`
    ).subscribe({
      next: () => {
        this.showToast(`Transfer request dispatched to ${this.selectedDestinationHospital.hospital.name}`);
        this.showTransferModal = false;
        this.loadTransfers();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.showToast('Transfer failed: ' + (err.error?.message || err.message));
      }
    });
  }

  // ── Admit Patient (auto-escalates to city-wide search if no bed) ───────
  openAdmitModal(item: any): void {
    this.admitTargetItem = item;
    this.admitBedType = 'ICU';
    this.showAdmitModal = true;
  }

  closeAdmitModal(): void {
    this.showAdmitModal = false;
    this.admitTargetItem = null;
  }

  submitAdmissionRequest(): void {
    if (!this.admitTargetItem || !this.currentDoctorId) return;
    const item = this.admitTargetItem;

    this.apiService.requestAdmission(
      item.patientId ?? null,
      item.patientName,
      this.selectedHospitalId,
      this.currentDoctorId,
      this.admitBedType
    ).subscribe({
      next: (admission) => {
        this.closeAdmitModal();
        this.loadAdmissions(this.selectedHospitalId);

        if (admission.status === 'RESERVED') {
          this.showToast(`Bed reserved for ${item.patientName} (${this.admitBedType}).`);
        } else {
          // No bed of this type was available at this hospital - this is the
          // "unavailability" case. Instead of a silent REQUESTED admission
          // sitting unactioned, automatically kick off the same city-wide
          // search used for ICU transfers so staff see alternatives right away.
          this.showToast(`No ${this.admitBedType} bed available here — searching nearby hospitals…`);
          this.triggerTransferModal(item, this.admitBedType);
        }
      },
      error: (err) => this.showToast('Admission request failed: ' + (err.error?.message || err.message))
    });
  }

  changeBedStatus(bedId: number, status: string): void {
    this.apiService.updateBedStatus(bedId, status).subscribe(() => {
      this.loadBeds(this.selectedHospitalId);
      this.loadHospitals();
      this.showToast(`Bed status updated to ${status}`);
    });
  }

  acceptTransfer(transferId: number): void {
    this.apiService.acceptTransfer(transferId).subscribe(() => {
      this.showToast(`Transfer #${transferId} accepted — target bed reserved`);
      this.loadTransfers();
      this.loadHospitals();
      this.loadBeds(this.selectedHospitalId);
    });
  }

  rejectTransfer(transferId: number): void {
    this.apiService.rejectTransfer(transferId).subscribe(() => {
      this.showToast(`Transfer #${transferId} rejected`);
      this.loadTransfers();
    });
  }

  openWhatIfModal(): void {
    this.showWhatIfModal = true;
    this.runSimulation();
  }

  runSimulation(): void {
    this.apiService.runWhatIfSimulation(this.whatIfInput).subscribe(res => {
      this.whatIfResult = res;
      this.cdr.markForCheck();
    });
  }

  showToast(msg: string): void {
    this.notificationAlert = msg;
    this.cdr.markForCheck();
    setTimeout(() => {
      this.notificationAlert = null;
      this.cdr.markForCheck();
    }, 4000);
  }

  // ── Onboard Doctor (admin view) ───────────────────────────────────────
  openAddDoctorModal(): void {
    this.showAddDoctorModal = true;
    this.addDoctorError = null;
    this.addDoctorSuccessMessage = null;
    this.newDoctorForm = {
      name: '',
      email: '',
      phone: '',
      departmentId: this.departments.length > 0 ? this.departments[0].id : null
    };
  }

  closeAddDoctorModal(): void {
    this.showAddDoctorModal = false;
  }

  submitAddDoctor(): void {
    if (!this.newDoctorForm.name || !this.newDoctorForm.email || !this.newDoctorForm.departmentId) {
      this.addDoctorError = 'Please fill all required fields';
      return;
    }

    this.addDoctorLoading = true;
    this.addDoctorError = null;
    this.addDoctorSuccessMessage = null;

    const payload = {
      name: this.newDoctorForm.name,
      email: this.newDoctorForm.email,
      phone: this.newDoctorForm.phone,
      hospitalId: this.selectedHospitalId,
      departmentId: this.newDoctorForm.departmentId,
      specialization: 'General', // Default, can be updated later
      password: 'ChangeMe123!' // Default password for new doctors
    };

    // Use ApiService to call POST /api/admin/onboard-doctor
    this.apiService.onboardDoctor(payload).subscribe({
      next: () => {
        this.addDoctorLoading = false;
        this.addDoctorSuccessMessage = 'Doctor successfully onboarded!';
        this.loadDoctors(this.selectedHospitalId);
        this.cdr.markForCheck();
        setTimeout(() => {
          this.closeAddDoctorModal();
        }, 2000);
      },
      error: (err: any) => {
        this.addDoctorLoading = false;
        this.addDoctorError = err.error?.message || err.message || 'Failed to onboard doctor.';
        this.cdr.markForCheck();
      }
    });
  }

  get roleBadge(): string {
    switch (this.userRole) {
      case 'PATIENT': return 'Patient';
      case 'DOCTOR': return 'Doctor';
      case 'HOSPITAL_ADMIN': return 'Hospital Admin';
      case 'CITY_ADMIN': return 'City Admin';
      default: return this.userRole;
    }
  }

  get totalCityBeds(): number {
    return this.hospitals.reduce((acc, h) => acc + (h.totalBeds || 0), 0);
  }

  get totalCityAvailableBeds(): number {
    return this.hospitals.reduce((acc, h) => acc + (h.availableBeds || 0), 0);
  }

  get totalCityIcuAvailable(): number {
    return this.hospitals.reduce((acc, h) => acc + (h.availableIcuBeds || 0), 0);
  }

  get waitingQueueCount(): number {
    return this.queue.filter(q => q.status === 'WAITING').length;
  }

  getStatusClass(status: string): string {
    const s = status?.toLowerCase().replace(/_/g, '-');
    return `status-${s}`;
  }
}
