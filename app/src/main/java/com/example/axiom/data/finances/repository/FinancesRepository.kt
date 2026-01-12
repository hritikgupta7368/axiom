package com.example.axiom.data.finances.repository

import android.util.Log
import com.example.axiom.data.finances.domain.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await

class FinancesRepository(private val db: FirebaseFirestore) {

    // --- Product Operations ---
    fun getProducts(): Flow<List<Product>> {
        return db.collection("products")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)
                        ?.copy(id = doc.id)
                }
            }
            .map { product -> product.filter { it.active } }
    }

    suspend fun addProduct(product: Product) {
        val ref = db.collection("products").document()
        val newProduct = product.copy(id = ref.id, createdAt = System.currentTimeMillis())
        ref.set(newProduct).await()
    }

    suspend fun updateProduct(product: Product) {
        db.collection("products").document(product.id)
            .set(product.copy(updatedAt = System.currentTimeMillis()))
            .await()
    }

    suspend fun deleteProduct(productId: String) {
        db.collection("products").document(productId)
            .update("active", false, "updatedAt", System.currentTimeMillis())
            .await()
    }

    // --- SupplierFirm Operations ---
    fun getSupplierFirms(): Flow<List<SupplierFirm>> {
        return db.collection("supplier_firms")
            .whereEqualTo("isActive", true)
            .snapshots()
            .map { it.toObjects<SupplierFirm>() }
    }

    suspend fun addSupplierFirm(supplier: SupplierFirm) {
        val ref = db.collection("supplier_firms").document()
        val newSupplier = supplier.copy(id = ref.id, createdAt = System.currentTimeMillis())
        ref.set(newSupplier).await()
    }

    suspend fun updateSupplierFirm(supplier: SupplierFirm) {
        db.collection("supplier_firms").document(supplier.id)
            .set(supplier.copy(updatedAt = System.currentTimeMillis()))
            .await()
    }

    suspend fun deleteSupplierFirm(supplierId: String) {
        db.collection("supplier_firms").document(supplierId)
            .update("isActive", false, "updatedAt", System.currentTimeMillis())
            .await()
    }


    // --- PurchaseRecord Operations ---
    fun getPurchaseRecords(): Flow<List<PurchaseRecord>> {
        return db.collection("purchase_records")
            .snapshots()
            .map { it.toObjects<PurchaseRecord>() }
    }

    suspend fun addPurchaseRecord(record: PurchaseRecord) {
        val ref = db.collection("purchase_records").document()
        val newRecord = record.copy(id = ref.id, createdAt = System.currentTimeMillis())
        ref.set(newRecord).await()
    }

    suspend fun updatePurchaseRecord(record: PurchaseRecord) {
        db.collection("purchase_records").document(record.id)
            .set(record.copy(updatedAt = System.currentTimeMillis()))
            .await()
    }

    suspend fun deletePurchaseRecord(recordId: String) {
        // Hard delete for records or soft delete if needed. Implementing hard delete for now.
        db.collection("purchase_records").document(recordId).delete().await()
    }

    // --- CustomerFirm Operations ---
    fun getCustomerFirms(): Flow<List<CustomerFirm>> {
         return db.collection("customer_firms")
             .snapshots()
             .map { snapshot ->
                 snapshot.documents.mapNotNull { doc ->
                     doc.toObject(CustomerFirm::class.java)
                         ?.copy(id = doc.id)
                 }
             }
             .map { firms -> firms.filter { it.active  } }
    }

    suspend fun addCustomerFirm(customer: CustomerFirm) {
        val ref = db.collection("customer_firms").document()
        val newCustomer = customer.copy(id = ref.id, createdAt = System.currentTimeMillis())
        ref.set(newCustomer).await()
    }

    suspend fun updateCustomerFirm(customer: CustomerFirm) {
        db.collection("customer_firms").document(customer.id)
            .set(customer.copy(updatedAt = System.currentTimeMillis()))
            .await()
    }

    suspend fun deleteCustomerFirm(customerId: String) {
        db.collection("customer_firms").document(customerId)
             .update("active", false, "updatedAt", System.currentTimeMillis())
            .await()
    }

    fun getSellerFirms(): Flow<List<SellerFirm>> {
        return db.collection("seller_firms")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(SellerFirm::class.java)
                        ?.copy(id = doc.id)
                }
            }
            .map { firms -> firms.filter { it.isActive } }
    }


    suspend fun addSellerFirm(seller: SellerFirm) {
        val ref = db.collection("seller_firms").document()
        val newSeller = seller.copy(id = ref.id, isActive = true,createdAt = System.currentTimeMillis())
        ref.set(newSeller).await()
    }

    suspend fun updateSellerFirm(seller: SellerFirm) {
        db.collection("seller_firms").document(seller.id)
            .set(seller.copy(updatedAt = System.currentTimeMillis()))
            .await()
    }

    suspend fun deleteSellerFirm(sellerId: String) {
        db.collection("seller_firms").document(sellerId)
            .update("isActive", false, "updatedAt", System.currentTimeMillis())
            .await()
    }

    // --- Invoice Operations ---
    fun getInvoices(): Flow<List<Invoice>> {
        return db.collection("invoices")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Invoice::class.java)
                        ?.copy(id = doc.id)
                }
            }
            .map { invoice -> invoice.filter { !it.deleted } }
    }



    // Add function to fetch a single invoice by ID
    suspend fun getInvoiceById(invoiceId: String): Invoice? {
        return try {


            val snapshot = db.collection("invoices")
                .document(invoiceId)
                .get()
                .await()



            val invoice = snapshot
                .toObject(Invoice::class.java)
                ?.copy(id = snapshot.id)


            if (invoice?.deleted == true) {
                Log.d("InvoiceRepo", "Invoice is marked deleted")
                null
            } else {
                invoice
            }

        } catch (e: Exception) {
            Log.e("InvoiceRepo", "Error fetching invoice", e)
            null
        }
    }



    // Add function to fetch a customer by ID
    suspend fun getCustomerById(customerId: String): CustomerFirm? {
        return try {
            val snapshot = db.collection("customer_firms").document(customerId).get().await()
            snapshot.toObject(CustomerFirm::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    suspend fun addInvoice(invoice: Invoice) {
        db.collection("invoices")
            .document(invoice.id)
            .set(invoice.copy(createdAt = System.currentTimeMillis()))
            .await()
    }

    suspend fun updateInvoice(invoice: Invoice) {
        db.collection("invoices").document(invoice.id)
            .set(invoice.copy(updatedAt = System.currentTimeMillis()))
            .await()
    }
    
    suspend fun softDeleteInvoice(invoiceId: String) {
        db.collection("invoices").document(invoiceId)
            .update("deleted", true, "deletedAt", System.currentTimeMillis())
            .await()
    }


}