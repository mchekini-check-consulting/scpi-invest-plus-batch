package fr.formationacademy.scpiinvestplusbatch.service;

import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDtoOut;
import fr.formationacademy.scpiinvestplusbatch.entity.Scpi;
import fr.formationacademy.scpiinvestplusbatch.mapper.ScpiMapper;
import fr.formationacademy.scpiinvestplusbatch.repository.ScpiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ScpiService {
    private final ScpiRepository scpiRepository;
    private final ScpiMapper scpiMapper;

    public ScpiService(ScpiRepository scpiRepository, ScpiMapper scpiMapper) {
        this.scpiRepository = scpiRepository;
        this.scpiMapper = scpiMapper;
    }

    public List<ScpiDtoOut> getScpis() {
        List<Scpi> scpis = scpiRepository.findAll();
        return scpiMapper.scpiToScpiDtoOut(scpis);
    }

    public ScpiDtoOut getScpiDetailsById(Integer id) {
        return scpiRepository.findById(id).map(scpiMapper::scpiToScpiDtoOut).orElse(null);
    }

    public List<ScpiDtoOut> getAllScpis() {
        return  scpiRepository.findAll()
                .stream()
                .map(scpiMapper :: scpiToScpiDtoOut)
                .toList();
    }

    public void deleteMissingScpis(Set<String> scpisInCsv) {
        List<Scpi> allScpis = scpiRepository.findAll();
        List<Scpi> scpisToDelete = allScpis.stream()
                .filter(scpi -> !scpisInCsv.contains(scpi.getName()))
                .toList();

        if (!scpisToDelete.isEmpty()) {
            log.info("Suppression de {} SCPIs absentes du fichier CSV.", scpisToDelete.size());
            scpiRepository.deleteAll(scpisToDelete);
        } else {
            log.info("Aucune SCPI à supprimer.");
        }
    }

}
