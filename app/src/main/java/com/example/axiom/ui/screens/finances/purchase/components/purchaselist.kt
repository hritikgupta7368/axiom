package com.example.axiom.ui.screens.finances.purchase.components

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PurchaseForm(
//    purchaseId: String? = null,
//    purchaseWithItems: PurchaseWithItems?,
//    isEditing: Boolean,
//    onSave: (PurchaseRecordEntity, List<PurchaseItemEntity>) -> Unit,
//    onCancel: () -> Unit
//) {
//
//    var hasSubmitted by remember { mutableStateOf(false) }
//
//    val items = remember { mutableStateListOf<PurchaseItemEntity>() }
//
//    var currentRecord by remember {
//        mutableStateOf(
//            purchaseWithItems?.record ?: PurchaseRecordEntity(
//                id = UUID.randomUUID().toString()
//            )
//        )
//    }
//
//    LaunchedEffect(purchaseWithItems, isEditing) {
//        if (isEditing && purchaseWithItems != null) {
//            currentRecord = purchaseWithItems.record
//
//            items.clear()
//            items.addAll(purchaseWithItems.items)
//        } else if (!isEditing) {
//            items.clear()
//        }
//    }
//
//    fun validate(): Boolean {
//        hasSubmitted = true
//        return currentRecord.supplierId.isNotBlank()
//    }
//
//    fun onSubmit() {
//        if (!validate()) return
//
//        val totalTaxable = items.sumOf { it.taxableAmount }
//
//        val finalRecord = currentRecord.copy(
//            totalTaxableAmount = totalTaxable,
//            grandTotal = totalTaxable,
////            updatedAt = if (isEditing) System.currentTimeMillis() else null
//        )
//
//        onSave(finalRecord, items)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState()),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//
//        Text(if (isEditing) "Edit Purchase" else "Add Purchase")
//
//        OutlinedTextField(
//            value = currentRecord.supplierInvoiceNumber,
//            onValueChange = {
//                currentRecord = currentRecord.copy(
//                    supplierInvoiceNumber = it
//                )
//            },
//            label = { Text("Invoice Number") }
//        )
//
//        OutlinedTextField(
//            value = currentRecord.deliveryCharge,
//            onValueChange = {
//                currentRecord = currentRecord.copy(
//                    supplierId = it
//                )
//            },
//            label = { Text("Supplier Id") }
//        )
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("Purchase Items")
//            IconButton(
//                onClick = {
//                    // open product selector
//                }
//            ) {
//                Icon(Icons.Default.Add, null)
//            }
//        }
//
//        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//
//            if (items.isEmpty()) {
//                Text("No items added")
//            } else {
//                items.forEach { item ->
//                    Card {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Column {
//                                Text("Product: ${item.productId}")
//                                Text("Qty: ${item.quantity}")
//                                Text("Amount: ${item.taxableAmount}")
//                            }
//
//                            IconButton(
//                                onClick = { items.remove(item) }
//                            ) {
//                                Icon(Icons.Default.Delete, null)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Button(
//                onClick = onCancel,
//                modifier = Modifier.weight(1f)
//            ) {
//                Text("Cancel")
//            }
//
//            Button(
//                onClick = { onSubmit() },
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(if (isEditing) "Update" else "Save")
//            }
//        }
//    }
//}