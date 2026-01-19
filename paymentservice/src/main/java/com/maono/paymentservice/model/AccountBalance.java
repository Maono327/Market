package com.maono.paymentservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("balance")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@ToString
public class AccountBalance {
    @Id
    @Column("id")
    private Long id;
    @Column("balance")
    private BigDecimal accountBalance;

    public void reduce(BigDecimal balance) {
        accountBalance = accountBalance.subtract(balance);
    }


    public AccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }
}
