package fr.formationacademy.scpiinvestbatch.processor;

import static org.junit.jupiter.api.Assertions.*;

import fr.formationacademy.scpiinvestplusapi.entity.Scpi;
import fr.formationacademy.scpiinvestplusapi.repository.ScpiRepository;
import fr.formationacademy.scpiinvestplusapi.service.LocationService;
import fr.formationacademy.scpiinvestplusapi.service.SectorService;
import fr.formationacademy.scpiinvestplusapi.service.StatYearService;
import org.junit.jupiter.api.Test;

import fr.formationacademy.scpiinvestplusapi.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusapi.dto.ScpiDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScpiItemProcessorTest {

    @Mock
    private ScpiRepository scpiRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private SectorService sectorService;

    @Mock
    private StatYearService statYearService;

    @InjectMocks
    private ScpiItemProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ScpiItemProcessor(scpiRepository, locationService, sectorService, statYearService);
    }

    @Test
    void testProcessWithValidBatchData() {
        ScpiDto scpiDto = ScpiDto.builder()
                .id(1)
                .name("Test SCPI")
                .minimumSubscription(1000)
                .manager("Test Manager")
                .capitalization(50000L)
                .subscriptionFees(BigDecimal.valueOf(2.5))
                .managementCosts(BigDecimal.valueOf(1.5))
                .enjoymentDelay(30)
                .iban("FR7630004000031234567890143")
                .bic("BNPAFRPPXXX")
                .scheduledPayment(true)
                .cashback(1000.0f)
                .advertising("Special Offer")
                .build();

        BatchDataDto batchDataDto = BatchDataDto.builder()
                .scpiDto(scpiDto)
                .build();

        Scpi result = processor.process(batchDataDto);

        assertNotNull(result);
        assertEquals("Test SCPI", result.getName());
        assertEquals(1000, result.getMinimumSubscription());
        assertEquals("Test Manager", result.getManager());
        assertEquals(50000L, result.getCapitalization(), "Capitalization incorrecte");
        assertEquals(BigDecimal.valueOf(2.5), result.getSubscriptionFees());
        assertEquals(BigDecimal.valueOf(1.5), result.getManagementCosts());
        assertEquals(30, result.getEnjoymentDelay());
        assertEquals("FR7630004000031234567890143", result.getIban());
        assertEquals("BNPAFRPPXXX", result.getBic());
        assertEquals(1000L, result.getCashback());
        assertEquals("Special Offer", result.getAdvertising());
    }

    @Test
    void testProcessNullBatchData() {
        BatchDataDto nullScpiDto = BatchDataDto.builder()
                .scpiDto(null)
                .build();

        assertNull(processor.process(nullScpiDto));

        verify(locationService, never()).createLocations(anyString(), any());
        verify(locationService, never()).saveLocations(any());
        verify(sectorService, never()).createSectors(anyString(), any());
        verify(sectorService, never()).saveSectors(any());
    }

    @Test
    void testProcessExistingScpiNeedsUpdate() {
        Scpi existingScpi = new Scpi();
        existingScpi.setName("Existing SCPI");
        existingScpi.setMinimumSubscription(1000);

        when(scpiRepository.findAll()).thenReturn(List.of(existingScpi));

        processor.init();

        ScpiDto scpiDto = ScpiDto.builder()
                .name("Existing SCPI")
                .minimumSubscription(2000)
                .build();

        BatchDataDto batchDataDto = BatchDataDto.builder()
                .scpiDto(scpiDto)
                .build();

        Scpi result = processor.process(batchDataDto);

        assertNotNull(result);
        assertEquals("Existing SCPI", result.getName());
        assertEquals(2000, result.getMinimumSubscription());
    }

    @Test
    void testInitLoadsExistingScpis() {
        Scpi scpi1 = new Scpi();
        scpi1.setName("SCPI 1");
        Scpi scpi2 = new Scpi();
        scpi2.setName("SCPI 2");

        when(scpiRepository.findAll()).thenReturn(List.of(scpi1, scpi2));

        processor.init();

        assertEquals(2, processor.existingScpis.size());
        assertTrue(processor.existingScpis.containsKey("SCPI 1"));
        assertTrue(processor.existingScpis.containsKey("SCPI 2"));
    }

}
