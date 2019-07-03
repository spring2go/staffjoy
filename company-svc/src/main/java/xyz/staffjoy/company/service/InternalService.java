package xyz.staffjoy.company.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.staffjoy.company.dto.GrowthGraphResponse;
import xyz.staffjoy.company.repo.ShiftRepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InternalService {
    @Autowired
    ShiftRepo shiftRepo;

    public GrowthGraphResponse getGrowthGraph() {
        // PeopleOnShifts returns the count of people working right now
        int peopleOnShifts = shiftRepo.getPeopleOnShifts();

        // ScheduledPerWeek returns the weekly number of shifts/week
        List<ShiftRepo.IScheduledPerWeek> scheduledPerWeekList = shiftRepo.getScheduledPerWeekList();

        Map<String, Integer> stuff = new HashMap<>();
        for(ShiftRepo.IScheduledPerWeek scheduledPerWeek : scheduledPerWeekList) {
            stuff.put(scheduledPerWeek.getWeek(), scheduledPerWeek.getCount());
        }

        GrowthGraphResponse response = GrowthGraphResponse.builder()
                .peopleScheduledPerWeek(stuff)
                .peopleOnShift(peopleOnShifts)
                .build();

        return response;
    }
}
