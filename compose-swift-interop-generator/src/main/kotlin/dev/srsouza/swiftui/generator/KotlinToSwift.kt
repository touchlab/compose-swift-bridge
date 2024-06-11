package dev.srsouza.swiftui.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.Dynamic
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import io.outfoxx.swiftpoet.ARRAY
import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.VOID
import io.outfoxx.swiftpoet.parameterizedBy
import io.outfoxx.swiftpoet.TypeName as SwiftTypeName

fun TypeName.toSwift(): SwiftTypeName? {
    return when(this) {
        is ClassName -> {
            toSwift()
        }
        is LambdaTypeName -> {
            FunctionTypeName.get(
                parameters = parameters.map { it.type.toSwift()!! }.toTypedArray(),
                returnType = returnType.toSwift()!!
            )
        }
        is ParameterizedTypeName -> {
            // TODO: 2. Map Kotlin types to Swift (List, Map)
            rawType.toSwift().parameterizedBy(
                *typeArguments.map { it.toSwift()!! }.toTypedArray()
            )
        }

        is TypeVariableName,
        is WildcardTypeName,
        Dynamic -> null
    }
}

fun ClassName.toSwift(): DeclaredTypeName {
    // TODO: support kotlin name mangling for Swift if there is one?
    //  maybe read `@ObjcName` from the type?
    //  this is a place where a SKIE SubPlugin would be able to get
    //  the right value.
    //  fix: Objc types will not have proper Module import

    // TODO: 2. Map Kotlin types to Swift

    fun type(name: String) = DeclaredTypeName.typeName(".$name")

    return when {
        simpleName == "Pair" && packageName == "kotlin" -> type("KotlinPair")
        simpleName in listOf("List", "MutableList") -> ARRAY
        simpleName == "Unit" -> VOID
        else -> type(simpleName)
    }
}