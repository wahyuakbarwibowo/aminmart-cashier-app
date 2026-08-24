package com.wahyuakbarwibowo.aminmartkasir.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("super_admin")
    SUPER_ADMIN,

    @SerialName("admin")
    ADMIN,

    @SerialName("kasir")
    KASIR;

    val label: String
        get() = when (this) {
            SUPER_ADMIN -> "Super Admin"
            ADMIN -> "Admin"
            KASIR -> "Kasir"
        }
}
