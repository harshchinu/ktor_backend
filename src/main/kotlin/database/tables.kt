package database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Factories : Table("factories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val address = varchar("address", 255)
    val contactInfo = varchar("contact_info", 255)
    override val primaryKey = PrimaryKey(id)
}

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 50)
    override val primaryKey = PrimaryKey(id)
}

object Manufacturers : Table("manufacturers") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val name = varchar("name", 255)
    val contactInfo = varchar("contact_info", 255)
    val address = varchar("address", 255)
    override val primaryKey = PrimaryKey(id)
}

object RawMaterials : Table("raw_materials") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val materialName = varchar("material_name", 255)
    val description = text("description")
    val unitOfMeasurement = varchar("unit_of_measurement", 50)
    val currentStock = double("current_stock").default(0.0)
    override val primaryKey = PrimaryKey(id)
}

object Products : Table("products") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val productName = varchar("product_name", 255)
    val description = text("description")
    val category = varchar("category", 100)
    override val primaryKey = PrimaryKey(id)
}

object ProductVariants : Table("product_variants") {
    val id = integer("id").autoIncrement()
    val productId = integer("product_id").references(Products.id)
    val variantName = varchar("variant_name", 50)
    val skuCode = varchar("sku_code", 100).uniqueIndex()
    val price = decimal("price", 10, 2)
    override val primaryKey = PrimaryKey(id)
}

object InwardRecords : Table("inward_records") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val manufacturerId = integer("manufacturer_id").references(Manufacturers.id)
    val materialId = integer("material_id").references(RawMaterials.id)
    val quantityReceived = double("quantity_received")
    val dateReceived = datetime("date_received")
    override val primaryKey = PrimaryKey(id)
}

object MasterTasks : Table("master_tasks") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val productId = integer("product_id").references(Products.id)
    val assignedMasterId = integer("assigned_master_id").references(Employees.id)
    val quantityAssigned = double("quantity_assigned")
    val cutDate = datetime("cut_date")
    val status = varchar("status", 50)
    override val primaryKey = PrimaryKey(id)
}

object CutVariants : Table("cut_variants") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val masterTaskId = integer("master_task_id").references(MasterTasks.id)
    val variantId = integer("variant_id").references(ProductVariants.id)
    val quantityCut = double("quantity_cut")
    override val primaryKey = PrimaryKey(id)
}

object Workflows : Table("workflows") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val workflowName = varchar("workflow_name", 255)
    val description = text("description")
    override val primaryKey = PrimaryKey(id)
}

object WorkflowTasks : Table("workflow_tasks") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val workflowId = integer("workflow_id").references(Workflows.id)
    val taskName = varchar("task_name", 255)
    val payRatePerUnit = decimal("pay_rate_per_unit", 10, 2)
    override val primaryKey = PrimaryKey(id)
}

object ProductStock : Table("product_stock") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val variantId = integer("variant_id").references(ProductVariants.id)
    val quantityInStock = double("quantity_in_stock")
    override val primaryKey = PrimaryKey(id)
}

object Payroll : Table("payroll") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val workerId = integer("worker_id").references(Employees.id)
    val taskId = integer("task_id").references(WorkflowTasks.id)
    val quantityCompleted = double("quantity_completed")
    val totalPay = decimal("total_pay", 10, 2)
    val dateReceived = datetime("date_received")
    override val primaryKey = PrimaryKey(id)
}

object AuditLogs : Table("audit_logs") {
    val id = integer("id").autoIncrement()
    val factoryId = integer("factory_id").references(Factories.id)
    val orderId = integer("order_id").nullable()
    val taskId = integer("task_id").nullable()
    val workerId = integer("worker_id").nullable()
    val action = varchar("action", 255)
    val timestamp = datetime("timestamp")
    override val primaryKey = PrimaryKey(id)
}

object Employees : Table() {
    val id = integer("employee_id").autoIncrement()
    val name = varchar("name", 255)
    val role = varchar("role", 50) // Example: "Worker", "Master"
    val contactInfo = varchar("contact_info", 255)
    val factoryId = integer("factory_id").references(Factories.id)
    override val primaryKey = PrimaryKey(id)
}

object Tasks : Table() {
    val id = integer("task_id").autoIncrement()
    val employeeId = integer("employee_id").references(Employees.id)
    val status = varchar("status", 50)
    val orderId = integer("order_id").references(Orders.id)
    override val primaryKey = PrimaryKey(id)
}

object Orders : Table() {
    val id = integer("order_id").autoIncrement()
    val workflowId = integer("workflow_id").references(Workflows.id)
    val productId = integer("product_id").references(Products.id)
    val cutVariantId = integer("cut_variant_id").references(CutVariants.id)
    val quantity = integer("quantity")
    val status = varchar("status", 50)
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}
