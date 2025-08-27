package se.dtime.service.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.config.EmailSendConfig;
import se.dtime.model.ActivationStatus;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.system.EmailSender;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchedulerTest {
    @InjectMocks
    private Scheduler scheduler;
    @Mock
    private EmailSendConfig emailSendConfig;
    @Mock
    private CalendarService calendarService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailSender emailSender;


    @Test
    public void emailReminderEmailReminderPropertyFalse() {
        when(emailSendConfig.isMailEnabled()).thenReturn(false);
        scheduler.emailReminder();
        verify(calendarService, never()).getNowDate();
    }

    @Test
    public void emailReminderEmailReminderNotLastDayOfMonth() {
        when(emailSendConfig.isMailEnabled()).thenReturn(true);

        LocalDate now = LocalDate.now();
        when(calendarService.getNowDate()).thenReturn(now);
        when(calendarService.getLastWorkingDayOfMonth(now)).thenReturn(now.minusDays(1));

        scheduler.emailReminder();
        verify(userRepository, never()).findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
    }

    @Test
    public void emailReminderEmailReminderLastDayOfMonth() {
        when(emailSendConfig.isMailEnabled()).thenReturn(true);

        LocalDate now = LocalDate.now();
        when(calendarService.getNowDate()).thenReturn(now);
        when(calendarService.getLastWorkingDayOfMonth(now)).thenReturn(now);

        when(userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE)).thenReturn(new ArrayList<>());

        scheduler.emailReminder();
        verify(userRepository, times(1)).findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
    }
}