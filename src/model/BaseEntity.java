package model;

import java.util.Objects;

/**
 * Abstract base for all persistent domain entities in ExpenseTracker.
 *
 * <h3>OOP role – Inheritance</h3>
 * <p>Every entity that is persisted to the database shares a single
 * surrogate primary key ({@code id}).  Centralising it here means:
 * <ul>
 *   <li>Sub-classes ({@link User}, {@link Expense}) inherit
 *       {@code getId()} / {@code setId()} without duplication.</li>
 *   <li>DAO and service code can accept {@code BaseEntity} parameters
 *       for generic operations (e.g. existence checks) – the basis
 *       for <em>polymorphism</em> demonstrated later.</li>
 * </ul>
 *
 * <p><b>No JDBC or UI code belongs here.</b>  This class is a pure
 * Java object (POJO) that is completely framework-agnostic.
 */
// Abstraction: Defines generic abstract entity template for persistent objects
// Inheritance: Base class extended by User and Expense
public abstract class BaseEntity {

    // Encapsulation: Private surrogate key protected from direct external modification
    private int id;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    /** No-arg constructor – required for reflection-based frameworks. */
    protected BaseEntity() {
        this.id = 0;
    }

    /**
     * Constructor used when loading an existing entity from the database.
     *
     * @param id the persisted primary-key value (must be &gt; 0)
     */
    protected BaseEntity(int id) {
        this.id = id;
    }

    // ----------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------

    /** @return the surrogate primary key, or {@code 0} if not yet persisted */
    public int getId() { return id; }

    /** @param id the primary key assigned by the database */
    public void setId(int id) { this.id = id; }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    /**
     * Two entities of the same concrete type are equal when their IDs
     * match and are both non-zero (i.e. both have been persisted).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != 0 && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
