package com.anubhav.devtools

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun main() {

    val person = Person()

    person.firstName = "ASEM"
    println(person.updatedCounter) // prints 1

    person.firstName = "ASEM"
    person.lastName = "ASEM"
    println(person.updatedCounter) // prints 3
}

class Person() {
    var firstName: String by PersonDelegate()
    var lastName: String by PersonDelegate()
    var updatedCounter: Int = 0
}

class PersonDelegate: ReadWriteProperty<Person, String> {
    private var value: String = ""

    override fun getValue(thisRef: Person, property: KProperty<*>): String {
        thisRef.updatedCounter++
        return value
    }

    override fun setValue(thisRef: Person, property: KProperty<*>, value: String) {
        thisRef.updatedCounter++
        this.value = value
    }
}