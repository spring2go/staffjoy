package xyz.staffjoy.company.service;

import org.springframework.stereotype.Service;
import xyz.staffjoy.company.dto.TimeZoneList;

import java.util.TimeZone;

@Service
public class TimeZoneService {

    public TimeZoneList listTimeZones() {
        TimeZoneList timeZoneList = TimeZoneList.builder().build();
        for(String id : TimeZone.getAvailableIDs()) {
            timeZoneList.getTimezones().add(id);
        }
        return timeZoneList;
    }
}
