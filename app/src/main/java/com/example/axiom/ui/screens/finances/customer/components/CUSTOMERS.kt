package com.example.axiom.ui.screens.finances.customer.components

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

enum class ContactType { PHONE, EMAIL, WEBSITE }
enum class PartyType {
    CUSTOMER,
    SUPPLIER,
    SELLER
}


@Entity(tableName = "party")
data class PartyEntity(
    @PrimaryKey val id: String = "",

    val partyType: PartyType = PartyType.CUSTOMER,

    //common fields
    val businessName: String = "",
    val logoUrl: String? = null,
    val registrationType: String = "GST", // B2B vs B2C
    val gstNumber: String? = null,
    val stateCode: String? = null,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    val isDeleted: Boolean = false,


    //Customer related
    val billingAddress: String? = null,
    val defaultShippingAddress: String? = null,
    val creditLimit: Double = 0.0,
    val openingBalance: Double = 0.0,


    //seller only
    val bankName: String? = null,
    val branchName: String? = "Sahibabad",
    val accountNumber: String? = null,
    val ifscCode: String? = null,
    val signatureUrl: String? = null,
    val stampUrl: String? = null,

    )

// One-to-Many Relationship: One Customer/Seller can have many contacts
@Entity(
    tableName = "party_contacts",
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["id"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("partyId")]
)
data class PartyContactEntity(
    @PrimaryKey val id: String = "",
    val partyId: String = "",
    val contactType: ContactType = ContactType.PHONE,
    val value: String = "",
    val isPrimary: Boolean = false
)


data class PartyWithContacts(
    @Embedded val party: PartyEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "partyId"
    )
    val contacts: List<PartyContactEntity>
)


@Dao
interface PartyDao {

    // -----------------------
    // CREATE / UPDATE
    // -----------------------

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(party: PartyEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(parties: List<PartyEntity>)

    // FIX: Standard update modifies the row without deleting it
    @Update
    suspend fun update(party: PartyEntity)

    // -----------------------
    // READ BY TYPE
    // -----------------------

    @Query(
        """
        SELECT * FROM party
        WHERE partyType = :type AND isDeleted = 0
        ORDER BY businessName ASC
    """
    )
    fun getByType(type: PartyType): Flow<List<PartyEntity>>

    @Query(
        """
        SELECT * FROM party
        WHERE id = :id
        LIMIT 1
    """
    )
    suspend fun getById(id: String): PartyEntity?

    @Query(
        """
        SELECT * FROM party
        WHERE partyType = :type AND isDeleted = 0
        AND businessName LIKE '%' || :query || '%'
        ORDER BY businessName ASC
    """
    )
    fun searchByType(type: PartyType, query: String): Flow<List<PartyEntity>>

    @Transaction
    @Query(
        """
    SELECT * FROM party
    WHERE partyType = :type AND isDeleted = 0
    ORDER BY businessName ASC
"""
    )
    fun getByTypeWithContacts(type: PartyType): Flow<List<PartyWithContacts>>


    @Transaction
    @Query(
        """
    SELECT * FROM party
    WHERE partyType = :type AND isDeleted = 0
    AND businessName LIKE '%' || :query || '%'
    ORDER BY businessName ASC
"""
    )
    fun searchByTypeWithContacts(
        type: PartyType,
        query: String
    ): Flow<List<PartyWithContacts>>

    // -----------------------
    // DELETE (SOFT DELETE)
    // -----------------------

    // FIX: Soft Delete updates the flag instead of removing the row
    @Query("UPDATE party SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("UPDATE party SET isDeleted = 1 WHERE id IN (:ids)")
    suspend fun softDeleteAll(ids: List<String>)

    // HARD DELETE: Kept just in case, but you should rarely use this
    @Query("DELETE FROM party WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM party WHERE id IN (:ids)")
    suspend fun deleteAll(ids: List<String>)

    // -----------------------
    // CONTACTS & UPSERTS
    // -----------------------

    @Transaction
    @Query("SELECT * FROM party WHERE id = :id LIMIT 1")
    suspend fun getPartyWithContacts(id: String): PartyWithContacts?


    @Transaction
    suspend fun insertPartyWithContacts(
        party: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        upsertPartyWithContacts(party, contacts)
    }


    @Transaction
    suspend fun upsertPartyWithContacts(
        party: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        // 1. Check if the party already exists
        val existingParty = getById(party.id)

        // 2. Safely Update or Insert without triggering REPLACE (which causes FK drops)
        if (existingParty != null) {
            update(party)
        } else {
            insert(party)
        }

        // 3. Handle Contacts (It is safe to delete/re-insert child tables like contacts)
        deleteAllContacts(party.id)
        contacts.forEach { contact ->
            insertContact(contact.copy(partyId = party.id))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: PartyContactEntity)

    @Query(
        """
        SELECT * FROM party_contacts
        WHERE partyId = :partyId
        ORDER BY isPrimary DESC
    """
    )
    fun getContacts(partyId: String): Flow<List<PartyContactEntity>>

    @Query("DELETE FROM party_contacts WHERE id = :id")
    suspend fun deleteContact(id: String)

    @Query("DELETE FROM party_contacts WHERE partyId = :partyId")
    suspend fun deleteAllContacts(partyId: String)

    // -----------------------
    // SUPPLIER PURCHASE SUMMARY
    // -----------------------

    @Query(
        """
        SELECT COALESCE(SUM(grandTotal), 0.0)
        FROM purchase_records
        WHERE supplierId = :supplierId
    """
    )
    suspend fun getTotalPurchasesForSupplier(supplierId: String): Double


    // --- EXPORT ---
    @Query("SELECT * FROM party")
    suspend fun exportAllParties(): List<PartyEntity>

    @Query("SELECT * FROM party_contacts")
    suspend fun exportAllContacts(): List<PartyContactEntity>

    // --- RESTORE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreParties(parties: List<PartyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreContacts(contacts: List<PartyContactEntity>)


}