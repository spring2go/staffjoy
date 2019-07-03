package xyz.staffjoy.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="account")
public class AccountSecret {
    @Id
    private String id;

    private String email;

    private boolean confirmedAndActive;

    private String passwordHash;
}
