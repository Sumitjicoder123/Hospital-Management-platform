import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from './services/api.service';

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
  departments: any[] = [];
  doctors: any[] = [];
  beds: any[] = [];
  queue: any[] = [];
  transfers: any[] = [];
  admissions: any[] = [];

  // Patient Booking Form
  patientView: string = 'DASHBOARD';
  patientTokens: any[] = [];
  patientHistoryRecords: any[] = [];
  bookingForm = {
    patientName: '',
    patientPhone: '',
    departmentId: null as number | null,
    doctorId: null as number | null,
    priority: 'NORMAL'
  };
  latestToken: any = null;

  // Doctor Workstation
  currentDoctorId: number = 1;
  selectedDoctor: any = null;

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
    weightKg: null as number | null
  };

  constructor(private apiService: ApiService, private cdr: ChangeDetectorRef, private router: Router) {}

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

  logout(): void {
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
        this.cdr.markForCheck();
      },
      error: (err) => console.error('Failed to load hospitals', err)
    });
  }

  loadPatientData(): void {
    if (this.currentUser && this.currentUser.phone) {
      this.apiService.getPatientQueue(this.currentUser.phone).subscribe(data => {
        this.patientTokens = data.sort((a, b) => b.id - a.id);
        this.cdr.markForCheck();
      });
      this.apiService.getPatientHistoryByPhone(this.currentUser.phone).subscribe(data => {
        this.patientHistoryRecords = data.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.cdr.markForCheck();
      });
    }
  }

  onHospitalSelect(hospitalId: number): void {
    this.selectedHospitalId = Number(hospitalId);
    this.apiService.getHospitalById(this.selectedHospitalId).subscribe(h => {
      this.selectedHospital = h;
      this.cdr.markForCheck();
    });
    this.loadDepartments(this.selectedHospitalId);
    this.loadDoctors(this.selectedHospitalId);
    this.loadBeds(this.selectedHospitalId);
    this.refreshQueue();
    this.loadAdmissions(this.selectedHospitalId);
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

  loadTransfers(): void {
    this.apiService.getAllTransfers().subscribe(data => {
      this.transfers = data;
      this.cdr.markForCheck();
    });
  }

  onDepartmentChange(): void {
    if (this.bookingForm.departmentId) {
      this.apiService.getDoctors(this.selectedHospitalId, this.bookingForm.departmentId).subscribe(docs => {
        this.doctors = docs;
        if (docs.length > 0) this.bookingForm.doctorId = docs[0].id;
        this.cdr.markForCheck();
      });
    }
  }

  submitBooking(): void {
    if (!this.bookingForm.doctorId || !this.bookingForm.departmentId) return;

    const payload = {
      patientId: this.currentUser.id,
      patientName: this.bookingForm.patientName,
      patientPhone: this.bookingForm.patientPhone,
      hospitalId: this.selectedHospitalId,
      departmentId: this.bookingForm.departmentId,
      doctorId: this.bookingForm.doctorId,
      priority: this.bookingForm.priority,
      bookingChannel: 'WEB'
    };

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

    const lookup = item.patientId
      ? this.apiService.getPatientHistory(item.patientId)
      : this.apiService.getPatientHistoryByPhone(item.patientPhone);

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
      weightKg: null
    };
    this.showRecordModal = true;
  }

  closeRecordModal(): void {
    this.showRecordModal = false;
    this.recordTargetItem = null;
  }

  submitConsultationRecord(): void {
    if (!this.recordTargetItem) return;
    const item = this.recordTargetItem;

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
      weightKg: this.recordForm.weightKg
    };

    this.apiService.createMedicalRecord(payload).subscribe({
      next: () => {
        // Only mark the queue entry COMPLETED once the visit record is saved,
        // so a doctor can never lose a consultation's history by mis-clicking.
        this.apiService.updateQueueStatus(item.id, 'COMPLETED').subscribe(() => {
          this.showToast(`Consultation completed for ${item.patientName}`);
          this.closeRecordModal();
          this.refreshQueue();
          this.cdr.markForCheck();
        });
      },
      error: (err) => this.showToast('Could not save visit record: ' + (err.error?.message || err.message))
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
