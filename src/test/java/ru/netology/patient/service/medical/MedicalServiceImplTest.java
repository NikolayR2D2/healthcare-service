package ru.netology.patient.service.medical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.alert.SendAlertServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;


public class MedicalServiceImplTest {
    static PatientInfoRepository testPatientInfoRepository;
    static SendAlertService testSendAlertService;
    static MedicalService testMedicalService;
    static PatientInfo testPatientInfo;
    static String testPatientId = "test-patient-id";
    static String testMessage = String.format("Warning, patient with id: %s, need help", testPatientId);
    static ArgumentCaptor<String> testArgumentCaptor;

    @BeforeEach
    void start() {
        testPatientInfoRepository = Mockito.mock(PatientInfoRepository.class);
        testSendAlertService = Mockito.mock(SendAlertService.class);
        testMedicalService = new MedicalServiceImpl(testPatientInfoRepository, testSendAlertService);
        testPatientInfo = new PatientInfo(
                testPatientId,
                "Иван",
                "Петров",
                LocalDate.of(1980, 11, 26),
                new HealthInfo(
                        new BigDecimal("36.65"),
                        new BloodPressure(120, 80)
                )
        );
        Mockito.when(testPatientInfoRepository.getById(testPatientId)).thenReturn(testPatientInfo);
        testArgumentCaptor = ArgumentCaptor.forClass(String.class);
    }

    @Test
    void warns_if_pressure_issue() {
        testMedicalService.checkBloodPressure(testPatientId, new BloodPressure(120, 80));
        Mockito.verify(testSendAlertService, Mockito.only()).send(testArgumentCaptor.capture());
        assertEquals(testMessage, testArgumentCaptor.getValue());
    }

    @Test
    void warns_if_temperature_issue() {
        testMedicalService.checkTemperature(testPatientId, new BigDecimal("35"));
        Mockito.verify(testSendAlertService, Mockito.only()).send(testArgumentCaptor.capture());
        assertEquals(testMessage, testArgumentCaptor.getValue());
    }

    @Test
    void no_warning_if_ok() {
        testMedicalService.checkBloodPressure(testPatientId, new BloodPressure(110, 70));
        testMedicalService.checkTemperature(testPatientId, new BigDecimal("40"));
        Mockito.verify(testSendAlertService, Mockito.never()).send(Mockito.any());
    }
}
