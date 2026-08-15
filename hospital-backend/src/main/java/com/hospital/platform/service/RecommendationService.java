package com.hospital.platform.service;

import com.hospital.platform.dto.OperationDTOs.HospitalMatchScoreDTO;
import com.hospital.platform.entity.Hospital;
import com.hospital.platform.entity.Ward;
import com.hospital.platform.repository.BedRepository;
import com.hospital.platform.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final HospitalRepository hospitalRepository;
    private final BedRepository bedRepository;

    public RecommendationService(HospitalRepository hospitalRepository, BedRepository bedRepository) {
        this.hospitalRepository = hospitalRepository;
        this.bedRepository = bedRepository;
    }

    public List<HospitalMatchScoreDTO> findAndRankHospitals(Long sourceHospitalId, Ward.BedType requiredBedType) {
        Hospital sourceHospital = hospitalRepository.findById(sourceHospitalId).orElse(null);
        double sourceLat = sourceHospital != null ? sourceHospital.getLatitude() : 28.6139;
        double sourceLng = sourceHospital != null ? sourceHospital.getLongitude() : 77.2090;

        List<Hospital> allHospitals = hospitalRepository.findAll();
        List<HospitalMatchScoreDTO> matches = new ArrayList<>();

        for (Hospital h : allHospitals) {
            if (h.getId().equals(sourceHospitalId)) continue; // skip current source hospital

            long availableBedsCount = bedRepository.countAvailableBedsByType(h.getId(), requiredBedType);
            if (availableBedsCount <= 0) continue; // Filter out hospitals with 0 available beds of required type

            double distKm = calculateHaversineDistance(sourceLat, sourceLng, h.getLatitude(), h.getLongitude());
            int etaMin = (int) Math.round((distKm / 35.0) * 60.0 + 3.0); // Assuming 35km/h avg traffic speed + 3min buffer

            // PRD Ranking Formula:
            // Score = (Available Beds * 25) - (Distance * 10) - (Travel Time * 2) + (Emergency Available ? 15 : 0)
            double score = (availableBedsCount * 25.0) - (distKm * 10.0) - (etaMin * 2.0) + (Boolean.TRUE.equals(h.getEmergencyAvailable()) ? 15.0 : 0.0);

            matches.add(new HospitalMatchScoreDTO(h, score, Math.round(distKm * 10.0) / 10.0, etaMin, availableBedsCount, ""));
        }

        // Sort descending by score
        matches.sort(Comparator.comparing(HospitalMatchScoreDTO::getScore).reversed());

        // Assign tags (Best Match, Fastest ETA, High Capacity)
        if (!matches.isEmpty()) {
            List<HospitalMatchScoreDTO> taggedList = new ArrayList<>();
            for (int i = 0; i < matches.size(); i++) {
                HospitalMatchScoreDTO item = matches.get(i);
                String tag = i == 0 ? "🥇 Best Match" : (item.getEstimatedTravelMinutes() <= 15 ? "⚡ Fastest ETA" : "🏥 Alternate Facility");
                taggedList.add(new HospitalMatchScoreDTO(
                        item.getHospital(),
                        item.getScore(),
                        item.getDistanceKm(),
                        item.getEstimatedTravelMinutes(),
                        item.getAvailableRequiredBeds(),
                        tag
                ));
            }
            return taggedList;
        }

        return matches;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
