import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Auth
  login(credentials: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/login`, credentials);
  }

  register(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/register`, data);
  }

  getMe(): Observable<any> {
    return this.http.get(`${this.baseUrl}/auth/me`);
  }

  // Hospitals
  getHospitals(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/hospitals`);
  }

  getHospitalById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/hospitals/${id}`);
  }

  getDepartments(hospitalId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/hospitals/${hospitalId}/departments`);
  }

  getDoctors(hospitalId: number, departmentId?: number): Observable<any[]> {
    let url = `${this.baseUrl}/hospitals/${hospitalId}/doctors`;
    if (departmentId) url += `?departmentId=${departmentId}`;
    return this.http.get<any[]>(url);
  }

  // OPD & Queues
  bookAppointment(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/appointments`, data);
  }

  getDoctorQueue(doctorId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/queues/doctor/${doctorId}`);
  }

  getHospitalQueue(hospitalId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/queues/hospital/${hospitalId}`);
  }

  getPatientQueue(phone: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/queues/patient?phone=${phone}`);
  }

  updateQueueStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/queues/${id}/status`, { status });
  }

  // Beds & Wards
  getBeds(hospitalId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/hospitals/${hospitalId}/beds`);
  }

  getWards(hospitalId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/hospitals/${hospitalId}/wards`);
  }

  reserveBed(bedId: number, patientId: number, patientName: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/beds/reserve`, { bedId, patientId, patientName });
  }

  updateBedStatus(bedId: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/beds/${bedId}/status?status=${status}`, {});
  }

  // Admissions
  getAdmissions(hospitalId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admissions/hospital/${hospitalId}`);
  }

  requestAdmission(patientId: number | null, patientName: string, hospitalId: number, doctorId: number, requiredBedType: string): Observable<any> {
    const patientIdParam = patientId != null ? `patientId=${patientId}&` : '';
    return this.http.post(`${this.baseUrl}/admissions/request?${patientIdParam}patientName=${encodeURIComponent(patientName)}&hospitalId=${hospitalId}&doctorId=${doctorId}&requiredBedType=${encodeURIComponent(requiredBedType)}`, {});
  }

  // Transfers & Recommendations
  getAllTransfers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/transfers`);
  }

  getTransfersForHospital(hospitalId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/transfers/hospital/${hospitalId}`);
  }

  getHospitalRecommendations(sourceHospitalId: number, requiredBedType: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/transfers/recommend?sourceHospitalId=${sourceHospitalId}&requiredBedType=${encodeURIComponent(requiredBedType)}`);
  }

  initiateTransfer(patientId: number | null, patientName: string, sourceHospitalId: number, destinationHospitalId: number, requiredBedType: string, urgency: string, notes: string): Observable<any> {
    const patientIdParam = patientId != null ? `patientId=${patientId}&` : '';
    return this.http.post(`${this.baseUrl}/transfers/initiate?${patientIdParam}patientName=${encodeURIComponent(patientName)}&sourceHospitalId=${sourceHospitalId}&destinationHospitalId=${destinationHospitalId}&requiredBedType=${encodeURIComponent(requiredBedType)}&urgency=${encodeURIComponent(urgency)}&notes=${encodeURIComponent(notes)}`, {});
  }

  acceptTransfer(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/transfers/${id}/accept`, {});
  }

  rejectTransfer(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/transfers/${id}/reject`, {});
  }

  // Analytics
  runWhatIfSimulation(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/analytics/what-if`, data);
  }

  // Medical Records / Patient History
  createMedicalRecord(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/medical-records`, data);
  }

  getPatientHistory(patientId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/medical-records/patient/${patientId}`);
  }

  getPatientHistoryByPhone(phone: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/medical-records/phone/${phone}`);
  }

  // WhatsApp bot - "simulate" lets you test the booking conversation from the
  // UI without a real WhatsApp number connected (see WhatsAppWebhookController).
  simulateWhatsAppMessage(phone: string, message: string): Observable<{ reply: string }> {
    return this.http.post<{ reply: string }>(`${this.baseUrl}/whatsapp/simulate`, { phone, message });
  }
}
