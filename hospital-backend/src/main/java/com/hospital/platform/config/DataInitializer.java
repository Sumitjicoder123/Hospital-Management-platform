package com.hospital.platform.config;

import com.hospital.platform.entity.*;
import com.hospital.platform.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           HospitalRepository hospitalRepository,
                           DepartmentRepository departmentRepository,
                           DoctorRepository doctorRepository,
                           WardRepository wardRepository,
                           BedRepository bedRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.wardRepository = wardRepository;
        this.bedRepository = bedRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Fix existing users that may have profileCompleted = false
        List<User> existingUsers = userRepository.findAll();
        for (User u : existingUsers) {
            if (u.getRole() != Role.PATIENT && !u.isProfileCompleted()) {
                u.setProfileCompleted(true);
                userRepository.save(u);
            }
        }

        if (hospitalRepository.count() > 0) {
            System.out.println("Database already seeded — skipping initialization.");
            return;
        }

        System.out.println("Seeding hospital, department, doctor, ward & bed data...");

        // System user accounts (for login)
        String pwd = passwordEncoder.encode("CityPulse@2026");
        userRepository.save(new User("City Admin", "cityadmin@citypulse.in", pwd, "+91 9800000000", Role.CITY_ADMIN, null));

        // ─────────────────────────────────────────────────────────────────────
        // HOSPITAL 1 — AIIMS New Delhi
        // ─────────────────────────────────────────────────────────────────────
        Hospital h1 = hospitalRepository.save(new Hospital(
                "AIIMS New Delhi",
                "Sri Aurobindo Marg, Ansari Nagar, New Delhi 110029",
                28.5672, 77.2100, "+91 11 2658 8500", true));

        Department h1_card = departmentRepository.save(new Department(h1.getId(), "Cardiology", "CARD"));
        Department h1_neuro = departmentRepository.save(new Department(h1.getId(), "Neurology", "NEUR"));
        Department h1_ortho = departmentRepository.save(new Department(h1.getId(), "Orthopedics", "ORTH"));
        Department h1_gastro = departmentRepository.save(new Department(h1.getId(), "Gastroenterology", "GAST"));
        Department h1_pulm = departmentRepository.save(new Department(h1.getId(), "Pulmonology", "PULM"));
        Department h1_emer = departmentRepository.save(new Department(h1.getId(), "Emergency Medicine", "EMER"));
        Department h1_peds = departmentRepository.save(new Department(h1.getId(), "Pediatrics", "PEDS"));
        Department h1_onco = departmentRepository.save(new Department(h1.getId(), "Oncology", "ONCO"));
        Department h1_derm = departmentRepository.save(new Department(h1.getId(), "Dermatology", "DERM"));
        Department h1_ent = departmentRepository.save(new Department(h1.getId(), "ENT (Otorhinolaryngology)", "ENTT"));
        Department h1_genmed = departmentRepository.save(new Department(h1.getId(), "General Medicine", "GMED"));
        Department h1_gensurg = departmentRepository.save(new Department(h1.getId(), "General Surgery", "GSUR"));

        doctorRepository.save(new Doctor(null, h1.getId(), h1_card.getId(), "Dr. Ramesh Gupta", "Interventional Cardiologist", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_card.getId(), "Dr. Sunita Verma", "Clinical Cardiologist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_neuro.getId(), "Dr. Arvind Mehta", "Neurologist", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_neuro.getId(), "Dr. Kavita Sharma", "Neuro-Physician", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_ortho.getId(), "Dr. Vikram Singh", "Orthopedic Surgeon", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_ortho.getId(), "Dr. Anil Kapoor", "Joint Replacement Specialist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_gastro.getId(), "Dr. Priya Nair", "Gastroenterologist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_pulm.getId(), "Dr. Sanjay Joshi", "Pulmonologist", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_emer.getId(), "Dr. Deepak Rawat", "Emergency Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_peds.getId(), "Dr. Meena Kumari", "Pediatrician", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_onco.getId(), "Dr. Rajesh Khanna", "Medical Oncologist", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_derm.getId(), "Dr. Neha Agarwal", "Dermatologist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_ent.getId(), "Dr. Ashok Kumar", "ENT Surgeon", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_genmed.getId(), "Dr. Suresh Patel", "General Physician", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h1.getId(), h1_gensurg.getId(), "Dr. Mohan Rao", "General Surgeon", Doctor.DoctorStatus.AVAILABLE, 7));

        createWardsAndBeds(h1.getId(), 10, 8, 40);

        // ─────────────────────────────────────────────────────────────────────
        // HOSPITAL 2 — Safdarjung Hospital
        // ─────────────────────────────────────────────────────────────────────
        Hospital h2 = hospitalRepository.save(new Hospital(
                "Safdarjung Hospital",
                "Ring Road, Safdarjung Enclave, New Delhi 110029",
                28.5685, 77.2050, "+91 11 2616 5060", true));

        Department h2_card = departmentRepository.save(new Department(h2.getId(), "Cardiology", "CARD"));
        Department h2_ortho = departmentRepository.save(new Department(h2.getId(), "Orthopedics", "ORTH"));
        Department h2_obgy = departmentRepository.save(new Department(h2.getId(), "Obstetrics & Gynecology", "OBGY"));
        Department h2_genmed = departmentRepository.save(new Department(h2.getId(), "General Medicine", "GMED"));
        Department h2_gensurg = departmentRepository.save(new Department(h2.getId(), "General Surgery", "GSUR"));
        Department h2_emer = departmentRepository.save(new Department(h2.getId(), "Emergency Medicine", "EMER"));
        Department h2_peds = departmentRepository.save(new Department(h2.getId(), "Pediatrics", "PEDS"));
        Department h2_derm = departmentRepository.save(new Department(h2.getId(), "Dermatology", "DERM"));

        doctorRepository.save(new Doctor(null, h2.getId(), h2_card.getId(), "Dr. Ananya Roy", "Cardiologist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h2.getId(), h2_ortho.getId(), "Dr. Rajan Tiwari", "Orthopedic Surgeon", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h2.getId(), h2_obgy.getId(), "Dr. Shalini Saxena", "Gynecologist & Obstetrician", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h2.getId(), h2_obgy.getId(), "Dr. Rekha Bhatt", "High-Risk Pregnancy Specialist", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h2.getId(), h2_genmed.getId(), "Dr. Manoj Pandey", "General Physician", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h2.getId(), h2_gensurg.getId(), "Dr. Vinod Mishra", "General & Laparoscopic Surgeon", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h2.getId(), h2_emer.getId(), "Dr. Alok Shukla", "Emergency Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h2.getId(), h2_peds.getId(), "Dr. Poonam Chandra", "Pediatrician", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h2.getId(), h2_derm.getId(), "Dr. Tanvi Malhotra", "Dermatologist & Cosmetologist", Doctor.DoctorStatus.AVAILABLE, 5));

        createWardsAndBeds(h2.getId(), 8, 6, 30);

        // ─────────────────────────────────────────────────────────────────────
        // HOSPITAL 3 — Sir Ganga Ram Hospital
        // ─────────────────────────────────────────────────────────────────────
        Hospital h3 = hospitalRepository.save(new Hospital(
                "Sir Ganga Ram Hospital",
                "Rajinder Nagar, New Delhi 110060",
                28.6400, 77.1870, "+91 11 2575 0000", true));

        Department h3_card = departmentRepository.save(new Department(h3.getId(), "Cardiology", "CARD"));
        Department h3_neuro = departmentRepository.save(new Department(h3.getId(), "Neurology", "NEUR"));
        Department h3_nephro = departmentRepository.save(new Department(h3.getId(), "Nephrology", "NEPH"));
        Department h3_uro = departmentRepository.save(new Department(h3.getId(), "Urology", "UROL"));
        Department h3_gastro = departmentRepository.save(new Department(h3.getId(), "Gastroenterology", "GAST"));
        Department h3_emer = departmentRepository.save(new Department(h3.getId(), "Emergency Medicine", "EMER"));
        Department h3_genmed = departmentRepository.save(new Department(h3.getId(), "General Medicine", "GMED"));
        Department h3_ent = departmentRepository.save(new Department(h3.getId(), "ENT (Otorhinolaryngology)", "ENTT"));

        doctorRepository.save(new Doctor(null, h3.getId(), h3_card.getId(), "Dr. Ashish Agrawal", "Cardiac Electrophysiologist", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h3.getId(), h3_card.getId(), "Dr. Nidhi Chaudhary", "Clinical Cardiologist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h3.getId(), h3_neuro.getId(), "Dr. Siddharth Jain", "Neurologist & Stroke Specialist", Doctor.DoctorStatus.AVAILABLE, 9));
        doctorRepository.save(new Doctor(null, h3.getId(), h3_nephro.getId(), "Dr. Rajeev Saran", "Nephrologist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h3.getId(), h3_uro.getId(), "Dr. Amitabh Sen", "Urologist & Robotic Surgeon", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h3.getId(), h3_gastro.getId(), "Dr. Pallavi Reddy", "Gastroenterologist & Hepatologist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h3.getId(), h3_emer.getId(), "Dr. Gaurav Tandon", "Emergency Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h3.getId(), h3_genmed.getId(), "Dr. Kiran Bedi", "Internal Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h3.getId(), h3_ent.getId(), "Dr. Harsh Vardhan", "ENT & Head-Neck Surgeon", Doctor.DoctorStatus.AVAILABLE, 6));

        createWardsAndBeds(h3.getId(), 8, 5, 25);

        // ─────────────────────────────────────────────────────────────────────
        // HOSPITAL 4 — Max Super Speciality Hospital, Saket
        // ─────────────────────────────────────────────────────────────────────
        Hospital h4 = hospitalRepository.save(new Hospital(
                "Max Super Speciality Hospital, Saket",
                "1, Press Enclave Road, Saket, New Delhi 110017",
                28.5275, 77.2133, "+91 11 2651 5050", true));

        Department h4_card = departmentRepository.save(new Department(h4.getId(), "Cardiac Sciences", "CARD"));
        Department h4_neuro = departmentRepository.save(new Department(h4.getId(), "Neurosciences", "NEUR"));
        Department h4_ortho = departmentRepository.save(new Department(h4.getId(), "Orthopedics & Joint Replacement", "ORTH"));
        Department h4_onco = departmentRepository.save(new Department(h4.getId(), "Oncology", "ONCO"));
        Department h4_nephro = departmentRepository.save(new Department(h4.getId(), "Nephrology & Renal Transplant", "NEPH"));
        Department h4_pulm = departmentRepository.save(new Department(h4.getId(), "Pulmonology & Chest Medicine", "PULM"));
        Department h4_emer = departmentRepository.save(new Department(h4.getId(), "Emergency Medicine", "EMER"));
        Department h4_genmed = departmentRepository.save(new Department(h4.getId(), "Internal Medicine", "GMED"));

        doctorRepository.save(new Doctor(null, h4.getId(), h4_card.getId(), "Dr. Vivek Tanwar", "Interventional Cardiologist", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_card.getId(), "Dr. Pooja Khosla", "Cardiac Surgeon", Doctor.DoctorStatus.AVAILABLE, 9));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_neuro.getId(), "Dr. Lalit Mohan", "Neurologist", Doctor.DoctorStatus.AVAILABLE, 9));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_ortho.getId(), "Dr. Devendra Yadav", "Joint Replacement Surgeon", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_ortho.getId(), "Dr. Rashmi Chauhan", "Sports Medicine & Arthroscopy", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_onco.getId(), "Dr. Sameer Kaul", "Surgical Oncologist", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_onco.getId(), "Dr. Meghna Srivastava", "Medical Oncologist", Doctor.DoctorStatus.AVAILABLE, 9));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_nephro.getId(), "Dr. Sunil Prakash", "Nephrologist & Transplant Physician", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_pulm.getId(), "Dr. Ritu Bansal", "Pulmonologist", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_emer.getId(), "Dr. Tarun Sahni", "Emergency & Critical Care Specialist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h4.getId(), h4_genmed.getId(), "Dr. Aditya Kapoor", "Internal Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 6));

        createWardsAndBeds(h4.getId(), 12, 6, 35);

        // ─────────────────────────────────────────────────────────────────────
        // HOSPITAL 5 — Fortis Escorts Heart Institute
        // ─────────────────────────────────────────────────────────────────────
        Hospital h5 = hospitalRepository.save(new Hospital(
                "Fortis Escorts Heart Institute",
                "Okhla Road, New Delhi 110025",
                28.5582, 77.2780, "+91 11 4713 5000", true));

        Department h5_card = departmentRepository.save(new Department(h5.getId(), "Cardiac Surgery", "CARD"));
        Department h5_ctsurg = departmentRepository.save(new Department(h5.getId(), "Cardiothoracic & Vascular Surgery", "CTVS"));
        Department h5_emer = departmentRepository.save(new Department(h5.getId(), "Emergency Medicine", "EMER"));
        Department h5_genmed = departmentRepository.save(new Department(h5.getId(), "Internal Medicine", "GMED"));
        Department h5_pulm = departmentRepository.save(new Department(h5.getId(), "Pulmonology", "PULM"));
        Department h5_endo = departmentRepository.save(new Department(h5.getId(), "Endocrinology & Diabetology", "ENDO"));

        doctorRepository.save(new Doctor(null, h5.getId(), h5_card.getId(), "Dr. Naresh Trehan", "Cardiac Surgeon", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h5.getId(), h5_card.getId(), "Dr. Aparna Jaswal", "Interventional Cardiologist", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h5.getId(), h5_ctsurg.getId(), "Dr. Zulfiqar Haq", "Cardiothoracic Surgeon", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h5.getId(), h5_emer.getId(), "Dr. Rahul Bhargava", "Emergency Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h5.getId(), h5_genmed.getId(), "Dr. Vijay Kumar", "General Physician", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h5.getId(), h5_pulm.getId(), "Dr. Neeraj Gupta", "Pulmonologist", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h5.getId(), h5_endo.getId(), "Dr. Ritika Samaddar", "Endocrinologist & Diabetologist", Doctor.DoctorStatus.AVAILABLE, 7));

        createWardsAndBeds(h5.getId(), 10, 6, 20);

        // ─────────────────────────────────────────────────────────────────────
        // HOSPITAL 6 — Lok Nayak Jai Prakash Narayan Hospital (LNJP)
        // ─────────────────────────────────────────────────────────────────────
        Hospital h6 = hospitalRepository.save(new Hospital(
                "Lok Nayak Hospital (LNJP)",
                "Jawaharlal Nehru Marg, Delhi Gate, New Delhi 110002",
                28.6387, 77.2395, "+91 11 2323 2400", true));

        Department h6_genmed = departmentRepository.save(new Department(h6.getId(), "General Medicine", "GMED"));
        Department h6_gensurg = departmentRepository.save(new Department(h6.getId(), "General Surgery", "GSUR"));
        Department h6_ortho = departmentRepository.save(new Department(h6.getId(), "Orthopedics", "ORTH"));
        Department h6_obgy = departmentRepository.save(new Department(h6.getId(), "Obstetrics & Gynecology", "OBGY"));
        Department h6_peds = departmentRepository.save(new Department(h6.getId(), "Pediatrics", "PEDS"));
        Department h6_emer = departmentRepository.save(new Department(h6.getId(), "Emergency Medicine", "EMER"));
        Department h6_derm = departmentRepository.save(new Department(h6.getId(), "Dermatology", "DERM"));
        Department h6_psych = departmentRepository.save(new Department(h6.getId(), "Psychiatry", "PSYC"));

        doctorRepository.save(new Doctor(null, h6.getId(), h6_genmed.getId(), "Dr. Kishore Tandon", "Internal Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h6.getId(), h6_genmed.getId(), "Dr. Nandini Sharma", "General Physician", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h6.getId(), h6_gensurg.getId(), "Dr. Ajay Bhatia", "General Surgeon", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h6.getId(), h6_ortho.getId(), "Dr. Manish Dhawan", "Trauma & Orthopedic Surgeon", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h6.getId(), h6_obgy.getId(), "Dr. Shweta Goyal", "Gynecologist & Obstetrician", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h6.getId(), h6_peds.getId(), "Dr. Anurag Mishra", "Pediatrician & Neonatologist", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h6.getId(), h6_emer.getId(), "Dr. Pawan Grover", "Emergency Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h6.getId(), h6_derm.getId(), "Dr. Sapna Rathi", "Dermatologist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h6.getId(), h6_psych.getId(), "Dr. Rajiv Sinha", "Psychiatrist", Doctor.DoctorStatus.AVAILABLE, 8));

        createWardsAndBeds(h6.getId(), 8, 6, 35);

        // ─────────────────────────────────────────────────────────────────────
        // HOSPITAL 7 — Apollo Hospital, Indraprastha
        // ─────────────────────────────────────────────────────────────────────
        Hospital h7 = hospitalRepository.save(new Hospital(
                "Indraprastha Apollo Hospital",
                "Mathura Road, Sarita Vihar, New Delhi 110076",
                28.5383, 77.2830, "+91 11 7179 1090", true));

        Department h7_card = departmentRepository.save(new Department(h7.getId(), "Cardiology & Cardiac Surgery", "CARD"));
        Department h7_neuro = departmentRepository.save(new Department(h7.getId(), "Neurology & Neurosurgery", "NEUR"));
        Department h7_ortho = departmentRepository.save(new Department(h7.getId(), "Orthopedics & Spine Surgery", "ORTH"));
        Department h7_onco = departmentRepository.save(new Department(h7.getId(), "Surgical Oncology", "ONCO"));
        Department h7_gastro = departmentRepository.save(new Department(h7.getId(), "Gastroenterology & GI Surgery", "GAST"));
        Department h7_transplant = departmentRepository.save(new Department(h7.getId(), "Organ Transplant", "TRNS"));
        Department h7_uro = departmentRepository.save(new Department(h7.getId(), "Urology & Andrology", "UROL"));
        Department h7_emer = departmentRepository.save(new Department(h7.getId(), "Emergency Medicine", "EMER"));
        Department h7_genmed = departmentRepository.save(new Department(h7.getId(), "Internal Medicine", "GMED"));
        Department h7_endo = departmentRepository.save(new Department(h7.getId(), "Endocrinology", "ENDO"));

        doctorRepository.save(new Doctor(null, h7.getId(), h7_card.getId(), "Dr. Rajiv Agarwal", "Interventional Cardiologist", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_card.getId(), "Dr. Mukesh Goel", "Cardiac Surgeon", Doctor.DoctorStatus.AVAILABLE, 9));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_neuro.getId(), "Dr. Vinay Goyal", "Neurologist", Doctor.DoctorStatus.AVAILABLE, 9));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_neuro.getId(), "Dr. Rana Patir", "Neurosurgeon", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_ortho.getId(), "Dr. Yash Gulati", "Orthopedic & Spine Surgeon", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_onco.getId(), "Dr. Harit Chaturvedi", "Surgical Oncologist", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_gastro.getId(), "Dr. Piyush Ranjan", "Gastroenterologist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_transplant.getId(), "Dr. Anupam Sibal", "Liver Transplant Specialist", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_uro.getId(), "Dr. Sudhir Chadha", "Urologist & Andrologist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_emer.getId(), "Dr. Nitin Jha", "Emergency Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_genmed.getId(), "Dr. Anupama Kaul", "Internal Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 6));
        doctorRepository.save(new Doctor(null, h7.getId(), h7_endo.getId(), "Dr. Ambrish Mithal", "Endocrinologist & Diabetologist", Doctor.DoctorStatus.AVAILABLE, 8));

        createWardsAndBeds(h7.getId(), 14, 8, 40);

        // ─────────────────────────────────────────────────────────────────────
        // HOSPITAL 8 — BLK-Max Super Speciality Hospital
        // ─────────────────────────────────────────────────────────────────────
        Hospital h8 = hospitalRepository.save(new Hospital(
                "BLK-Max Super Speciality Hospital",
                "Pusa Road, Rajinder Nagar, New Delhi 110005",
                28.6443, 77.1840, "+91 11 3040 3040", true));

        Department h8_card = departmentRepository.save(new Department(h8.getId(), "Cardiology", "CARD"));
        Department h8_neuro = departmentRepository.save(new Department(h8.getId(), "Neurosciences", "NEUR"));
        Department h8_ortho = departmentRepository.save(new Department(h8.getId(), "Bone & Joint Institute", "ORTH"));
        Department h8_onco = departmentRepository.save(new Department(h8.getId(), "Cancer Centre", "ONCO"));
        Department h8_nephro = departmentRepository.save(new Department(h8.getId(), "Nephrology & Dialysis", "NEPH"));
        Department h8_emer = departmentRepository.save(new Department(h8.getId(), "Emergency & Trauma", "EMER"));
        Department h8_genmed = departmentRepository.save(new Department(h8.getId(), "Internal Medicine", "GMED"));

        doctorRepository.save(new Doctor(null, h8.getId(), h8_card.getId(), "Dr. Sudheer Saxena", "Interventional Cardiologist", Doctor.DoctorStatus.AVAILABLE, 8));
        doctorRepository.save(new Doctor(null, h8.getId(), h8_neuro.getId(), "Dr. Manish Vaish", "Neurosurgeon", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h8.getId(), h8_ortho.getId(), "Dr. Aashish Chaudhry", "Joint Replacement Surgeon", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h8.getId(), h8_onco.getId(), "Dr. Hari Goyal", "Medical Oncologist", Doctor.DoctorStatus.AVAILABLE, 9));
        doctorRepository.save(new Doctor(null, h8.getId(), h8_onco.getId(), "Dr. Kapil Kumar", "Surgical Oncologist", Doctor.DoctorStatus.AVAILABLE, 10));
        doctorRepository.save(new Doctor(null, h8.getId(), h8_nephro.getId(), "Dr. Ajay Sharma", "Nephrologist", Doctor.DoctorStatus.AVAILABLE, 7));
        doctorRepository.save(new Doctor(null, h8.getId(), h8_emer.getId(), "Dr. Sandeep Dewan", "Emergency Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 5));
        doctorRepository.save(new Doctor(null, h8.getId(), h8_genmed.getId(), "Dr. Sandeep Budhiraja", "Internal Medicine Specialist", Doctor.DoctorStatus.AVAILABLE, 6));

        createWardsAndBeds(h8.getId(), 10, 6, 30);

        Hospital[] hospitals = {h1, h2, h3, h4, h5, h6, h7, h8};
        for (int i = 0; i < hospitals.length; i++) {
            int n = i + 1;
            Hospital h = hospitals[i];
            userRepository.save(new User("Admin - " + h.getName(), "admin.h" + n + "@citypulse.in", pwd, "+91 980000001" + n, Role.HOSPITAL_ADMIN, h.getId()));

            // Create a specific login account for EVERY doctor in the hospital
            List<Doctor> hospitalDoctors = doctorRepository.findByHospitalId(h.getId());
            int docIndex = 1;
            for (Doctor doc : hospitalDoctors) {
                // Generate a clean email (e.g. "Dr. Ashok Kumar" -> "ashok.kumar.h1@citypulse.in")
                String cleanName = doc.getName().toLowerCase().replace("dr. ", "").replace(" ", ".");
                String docEmail = cleanName + ".h" + n + "@citypulse.in";
                
                // Generate a unique dummy phone number for each doctor
                String docPhone = "+91 88" + String.format("%02d", n) + String.format("%04d", docIndex++);
                
                userRepository.save(new User(doc.getName(), docEmail, pwd, docPhone, Role.DOCTOR, h.getId(), doc.getId()));
            }
        }

        System.out.println("=== Database seeding complete ===");
        System.out.println("  Hospitals:   " + hospitalRepository.count());
        System.out.println("  Departments: " + departmentRepository.count());
        System.out.println("  Doctors:     " + doctorRepository.count());
        System.out.println("  Wards:       " + wardRepository.count());
        System.out.println("  Beds:        " + bedRepository.count());
    }

    /**
     * Creates ICU, Emergency, and General wards + beds for a hospital.
     * All beds start as AVAILABLE (no patients pre-loaded).
     */
    private void createWardsAndBeds(Long hospitalId, int icuBeds, int emergencyBeds, int generalBeds) {
        // ICU Ward
        Ward icuWard = wardRepository.save(new Ward(hospitalId, "Intensive Care Unit", Ward.BedType.ICU, icuBeds));
        for (int i = 1; i <= icuBeds; i++) {
            bedRepository.save(new Bed(icuWard.getId(), hospitalId, "ICU-" + i, Ward.BedType.ICU, Bed.BedStatus.AVAILABLE));
        }

        // Emergency Ward
        Ward emerWard = wardRepository.save(new Ward(hospitalId, "Emergency Ward", Ward.BedType.EMERGENCY, emergencyBeds));
        for (int i = 1; i <= emergencyBeds; i++) {
            bedRepository.save(new Bed(emerWard.getId(), hospitalId, "EMER-" + i, Ward.BedType.EMERGENCY, Bed.BedStatus.AVAILABLE));
        }

        // General Ward
        Ward genWard = wardRepository.save(new Ward(hospitalId, "General Ward", Ward.BedType.GENERAL, generalBeds));
        for (int i = 1; i <= generalBeds; i++) {
            bedRepository.save(new Bed(genWard.getId(), hospitalId, "GEN-" + i, Ward.BedType.GENERAL, Bed.BedStatus.AVAILABLE));
        }
    }
}
