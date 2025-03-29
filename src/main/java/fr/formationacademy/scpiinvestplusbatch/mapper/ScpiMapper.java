package fr.formationacademy.scpiinvestplusbatch.mapper;

import fr.formationacademy.scpiinvestplusbatch.dto.LocationDtoOut;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDtoOut;
import fr.formationacademy.scpiinvestplusbatch.dto.SectorDtoOut;
import fr.formationacademy.scpiinvestplusbatch.dto.StatYearDtoOut;
import fr.formationacademy.scpiinvestplusbatch.entity.Location;
import fr.formationacademy.scpiinvestplusbatch.entity.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.Sector;
import fr.formationacademy.scpiinvestplusbatch.entity.StatYear;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;


@Mapper(componentModel = "spring")
@Component
public interface ScpiMapper {

    @Mapping(target = "statYear", source = "statYears", qualifiedByName = "firstStatYear")
    @Mapping(target = "location", source = "locations", qualifiedByName = "highestPercentageLocation")
    @Mapping(target = "sector", source = "sectors", qualifiedByName = "highestPercentageSector")
    ScpiDtoOut scpiToScpiDtoOut(Scpi scpi);
    List<ScpiDtoOut> scpiToScpiDtoOut(List<Scpi> scpis);
    @Named("firstStatYear")
    default StatYearDtoOut getFirstStatYear(List<StatYear> statYears) {
        return (statYears != null && !statYears.isEmpty())
                ? StatYearDtoOut
                .builder()
                .yearStat(statYears.get(0).getYearStat())
                .distributionRate(statYears.get(0).getDistributionRate())
                .reconstitutionValue(statYears.get(0).getReconstitutionValue())
                .sharePrice(statYears.get(0).getSharePrice())
                .build()
                : null;
    }

    @Named("highestPercentageLocation")
    default LocationDtoOut getHighestPercentageLocation(List<Location> locations) {
        return (locations != null && !locations.isEmpty())
                ? locations.stream()
                .filter(loc -> loc.getCountryPercentage() != null) // Filtrer les valeurs nulles
                .max(Comparator.comparing(Location::getCountryPercentage)) // Comparaison directe
                .map(item -> LocationDtoOut
                        .builder()
                        .id(item.getId())
                        .countryPercentage(item.getCountryPercentage()) // Pas besoin de conversion
                        .build())
                .orElse(null)
                : null;
    }

    @Named("highestPercentageSector")
    default SectorDtoOut getHighestPercentageSector(List<Sector> sectors) {
        return (sectors != null && !sectors.isEmpty())
                ? sectors.stream()
                .max(Comparator.comparing(Sector::getSectorPercentage))
                .map(sector -> SectorDtoOut
                        .builder()
                        .id(sector.getId())
                        .sectorPercentage(sector.getSectorPercentage())
                        .build())
                .orElse(null)
                : null;
    }
}
