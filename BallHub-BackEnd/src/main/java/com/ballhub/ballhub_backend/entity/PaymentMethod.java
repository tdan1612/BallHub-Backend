package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PaymentMethods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentMethodID")
    private Integer paymentMethodId;

    @Column(name = "MethodName", length = 100)
    private String methodName;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "paymentMethod", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
