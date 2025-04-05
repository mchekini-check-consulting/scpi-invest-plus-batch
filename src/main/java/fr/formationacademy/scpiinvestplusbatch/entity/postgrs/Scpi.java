package fr.formationacademy.scpiinvestplusbatch.entity.postgrs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "scpi", uniqueConstraints = { @UniqueConstraint(columnNames = "name") })
public class Scpi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private Integer minimumSubscription;
    private String manager;
    private Long capitalization;
    private BigDecimal subscriptionFees;
    private BigDecimal managementCosts;
    private Integer enjoymentDelay;

    @Column(unique = true)
    private String iban;

    private String bic;

    private Boolean scheduledPayment;
    private String frequencyPayment;
    private Float cashback;
    private String advertising;

    @OneToMany(mappedBy = "scpi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Location> locations;

    @OneToMany(mappedBy = "scpi", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Sector> sectors;

    @OneToMany(mappedBy = "scpi", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<StatYear> statYears;

}
