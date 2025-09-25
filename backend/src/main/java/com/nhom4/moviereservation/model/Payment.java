package com.nhom4.moviereservation.model;

import java.time.LocalDateTime;

import com.nhom4.moviereservation.model.enums.PaymentMethod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments",
       indexes = {
           @Index(name = "fk_payments_user_id", columnList = "user_id"),
           @Index(name = "fk_payment_show_id", columnList = "show_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer id;
    private Integer amount;

    @Column(name = "payment_datetime")
    private LocalDateTime paymentDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod method;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_payments_user_id"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "show_id", foreignKey = @ForeignKey(name = "fk_payment_show_id"))
    private Show show;

    // Getters and setters
}