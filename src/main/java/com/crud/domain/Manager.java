package com.crud.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A Manager.
 */
@Entity
@Table(name = "manager")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Manager implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Min(value = 20)
    @Max(value = 50)
    @Column(name = "age")
    private Integer age;

    @NotNull
    @Column(name = "gender", nullable = false)
    private String gender;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "manager")
    @JsonIgnoreProperties(value = { "manager" }, allowSetters = true)
    private Set<Workers> ids = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Manager id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Manager name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return this.age;
    }

    public Manager age(Integer age) {
        this.setAge(age);
        return this;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return this.gender;
    }

    public Manager gender(String gender) {
        this.setGender(gender);
        return this;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Set<Workers> getIds() {
        return this.ids;
    }

    public void setIds(Set<Workers> workers) {
        if (this.ids != null) {
            this.ids.forEach(i -> i.setManager(null));
        }
        if (workers != null) {
            workers.forEach(i -> i.setManager(this));
        }
        this.ids = workers;
    }

    public Manager ids(Set<Workers> workers) {
        this.setIds(workers);
        return this;
    }

    public Manager addId(Workers workers) {
        this.ids.add(workers);
        workers.setManager(this);
        return this;
    }

    public Manager removeId(Workers workers) {
        this.ids.remove(workers);
        workers.setManager(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Manager)) {
            return false;
        }
        return getId() != null && getId().equals(((Manager) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Manager{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", age=" + getAge() +
            ", gender='" + getGender() + "'" +
            "}";
    }
}
