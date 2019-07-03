package xyz.staffjoy.ical.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.staffjoy.company.dto.ShiftDto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cal {

    static final String CAL_DATE_PATTERN = "yyyyMMdd'T'HHmmssZ";
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(CAL_DATE_PATTERN)
            .withZone(ZoneId.systemDefault());

    private String companyName;
    private List<ShiftDto> shiftList;

    private String getCalDateFormat(Instant dt) {
        return DATE_TIME_FORMATTER.format(dt);
    }

    public String getHeader() {
        StringBuilder header = new StringBuilder();
        header.append("BEGIN:VCALENDAR\r\n");
        header.append("METHOD:PUBLISH\r\n");
        header.append("VERSION:2.0\r\n");
        header.append("PRODID:-//Staffjoy//Staffjoy ICal Service//EN\r\n");
        return header.toString();
    }

    public String getBody() {
        StringBuilder body = new StringBuilder();
        for (ShiftDto shiftDto : shiftList) {
            body.append("BEGIN:VEVENT\r\n");
            body.append("ORGANIZER;CN=Engineering:MAILTO:support@staffjoy.xyz\r\n");
            body.append("SUMMARY: Work at " + this.companyName + "\r\n");
            body.append("UID:" + shiftDto.getUserId() + "\r\n");
            body.append("STATUS:CONFIRMED\r\n");
            body.append("DTSTART:" + getCalDateFormat(shiftDto.getStart()) + "\r\n");
            body.append("DTEND:" + getCalDateFormat(shiftDto.getStop()) + "\r\n");
            body.append("DTSTAMP:" + getCalDateFormat(Instant.now()) + "\r\n");
            body.append("LAST-MODIFIED:" + getCalDateFormat(Instant.now()) + "\r\n");
            body.append("LOCATION:  " + this.companyName + "\r\n");
            body.append("END:VEVENT\r\n");
        }
        return body.toString();
    }

    public String getFooter() {
        return "END:VCALENDAR";
    }

    // Build concats an ical header/body/footer together
    public String build() {
        return this.getHeader() + this.getBody() + this.getFooter();
    }
}
