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
enum class GstRegistrationType { REGISTERED, COMPOSITION, UNREGISTERED }
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
    val registrationType: GstRegistrationType = GstRegistrationType.REGISTERED, // B2B vs B2C
    val gstNumber: String? = null,
    val stateCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pinCode: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,


    //Customer related
    val billingAddress: String? = null,
    val defaultShippingAddress: String? = null,
    val creditLimit: Double = 0.0,
    val openingBalance: Double = 0.0,


    //seller only
    val bankName: String? = null,
    val branchName: String? = null,
    val accountNumber: String? = null,
    val ifscCode: String? = null,
    val upiId: String? = null,
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(party: PartyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parties: List<PartyEntity>)

    @Update
    suspend fun update(party: PartyEntity)

    // -----------------------
    // READ BY TYPE
    // -----------------------

    @Query(
        """
        SELECT * FROM party
        WHERE partyType = :type
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
        WHERE partyType = :type
        AND businessName LIKE '%' || :query || '%'
        ORDER BY businessName ASC
    """
    )
    fun searchByType(type: PartyType, query: String): Flow<List<PartyEntity>>

    // -----------------------
    // DELETE
    // -----------------------

    @Query("DELETE FROM party WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM party WHERE id IN (:ids)")
    suspend fun deleteAll(ids: List<String>)

    // -----------------------
    // CONTACTS
    // -----------------------


    @Transaction
    suspend fun insertPartyWithContacts(
        party: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        insert(party)
        deleteAllContacts(party.id)
        contacts.forEach { insertContact(it) }
    }

    @Transaction
    @Query("SELECT * FROM party WHERE id = :id LIMIT 1")
    suspend fun getPartyWithContacts(id: String): PartyWithContacts?

    @Transaction
    suspend fun upsertPartyWithContacts(
        party: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        insert(party)

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
}