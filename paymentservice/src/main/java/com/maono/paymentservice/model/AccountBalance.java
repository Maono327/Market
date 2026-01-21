package com.maono.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
    @Column("balance")
    private BigDecimal accountBalance;

    public void reduce(BigDecimal balance) {
        accountBalance = accountBalance.subtract(balance);
    }
}
