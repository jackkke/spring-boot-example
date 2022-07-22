package top.jackkke.jasypt.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author jackkke
 */
@RestController
public class TestController {

    @Value("${encode.msg}")
    private String msg;

    /**
     Examples:
         "PT20.345S" -- parses as "20.345 seconds"
         "PT15M"     -- parses as "15 minutes" (where a minute is 60 seconds)
         "PT10H"     -- parses as "10 hours" (where an hour is 3600 seconds)
         "P2D"       -- parses as "2 days" (where a day is 24 hours or 86400 seconds)
         "P2DT3H4M"  -- parses as "2 days, 3 hours and 4 minutes"
         "P-6H3M"    -- parses as "-6 hours and +3 minutes"
         "-P6H3M"    -- parses as "-6 hours and -3 minutes"
         "-P-6H+3M"  -- parses as "+6 hours and -3 minutes"
     */
    @Value("${date}")
    private Duration date = Duration.ofDays(1);

    @GetMapping("get")
    public String getMsg() {
        System.out.println("msg = " + msg);
        long seconds = date.getSeconds();
        System.out.println("seconds = " + seconds);
        System.out.println("date = " + date);
        return msg;
    }
}
