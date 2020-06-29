package se.dtime.service.scheduler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.ActivationStatus;
import se.dtime.repository.SystemPropertyRepository;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.system.EmailSender;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerTest {
    @InjectMocks
    private Scheduler scheduler;
    @Mock
    private SystemPropertyRepository systemPropertyRepository;
    @Mock
    private CalendarService calendarService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailSender emailSender;

    @Test
    public void emailReminderEmailReminderPropertyNoSpecified() {
        scheduler.emailReminder();
        verify(calendarService, never()).getNowDate();
    }

    @Test
    public void emailReminderEmailReminderPropertyFalse() {
        SystemPropertyPO systemPropertyPO = new SystemPropertyPO();
        systemPropertyPO.setValue("false");
        when(systemPropertyRepository.findByName(Scheduler.EMAIL_REMINDER_PROPERTY)).thenReturn(systemPropertyPO);

        scheduler.emailReminder();
        verify(calendarService, never()).getNowDate();
    }

    @Test
    public void emailReminderEmailReminderNotLastDayOfMonth() {
        SystemPropertyPO systemPropertyPO = new SystemPropertyPO();
        systemPropertyPO.setValue("true");
        when(systemPropertyRepository.findByName(Scheduler.EMAIL_REMINDER_PROPERTY)).thenReturn(systemPropertyPO);

        LocalDate now = LocalDate.now();
        when(calendarService.getNowDate()).thenReturn(now);
        when(calendarService.getLastWorkingDayOfMonth(now)).thenReturn(now.minusDays(1));

        scheduler.emailReminder();
        verify(userRepository, never()).findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
    }

    @Test
    public void emailReminderEmailReminderLastDayOfMonth() {
        SystemPropertyPO systemPropertyPO = new SystemPropertyPO();
        systemPropertyPO.setValue("true");
        when(systemPropertyRepository.findByName(Scheduler.EMAIL_REMINDER_PROPERTY)).thenReturn(systemPropertyPO);

        LocalDate now = LocalDate.now();
        when(calendarService.getNowDate()).thenReturn(now);
        when(calendarService.getLastWorkingDayOfMonth(now)).thenReturn(now);

        when(userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE)).thenReturn(new ArrayList<>());

        scheduler.emailReminder();
        verify(userRepository, times(1)).findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
    }
}