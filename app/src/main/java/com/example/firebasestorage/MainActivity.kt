package com.example.firebasestorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val  personCollectionRef = Firebase.firestore.collection("Persons")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Save Data
        btnSaveToDatabase.setOnClickListener{

            val persons = getOldPerson()
            savePerson(persons)

        }
        // Retrieve Data

        btnRetrieveData.setOnClickListener{
            retrievePersons()
        }

        subscribeToRealtimeUpdates()

        // Update Person in UI layout

        btnUpdatePerson.setOnClickListener{
            val oldPersons = getOldPerson()
            val newPersonMap = getNewPersonMap()
            updatePerson(oldPersons,newPersonMap)
        }
        //delete

        btnDelete.setOnClickListener{
            val persons = getOldPerson()
            deletePerson(persons)
        }
        btnBatchWrite.setOnClickListener{
            changeName("4mW2AtxFGENAg0xshFUc","hello","kumar")
        }

        btnTransaction.setOnClickListener{
            birthday("7acvcS9ItXXOli7Ki4Zo")
        }

    }
    // RealTimeDatabase

//    Error
    private fun subscribeToRealtimeUpdates() {

        personCollectionRef.addSnapshotListener{    // snapshotListener trigger the data and display in UI
            querySnapshot, firebaseFirestoreException ->    // we are checking firestore exception is not null
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val sb = StringBuilder()  // string Builder is a mutable character wheich we can insert , delete and update.
                for (document in it){  // use for loop to store document one by one in persons objects to show in Ui layout
                    val persons = document.toObject<Persons>() // we take default parameter -> person class for document
                    sb.append("$persons\n")
                }
                PersonData.text = sb.toString()
            }
        }

    }


    private fun savePerson(persons: Persons) = CoroutineScope(Dispatchers.IO).launch { // IO thread working in background thread -> read and write the database
        try{

            personCollectionRef.add(persons).await()  // we add the data in firebase database
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,"Successfully saved data!",Toast.LENGTH_SHORT).show()
            }
        }
        catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getOldPerson(): Persons{

        val firstName = etFirstName.text.toString()
        val lastName = etLastName.text.toString()
        val age = etAge.text.toString().toInt()
        val image = ivImage.setImageResource(R.drawable.shazam_logo).toString()
        return Persons(firstName,lastName,age,image)

    }

    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        val fromAge = etFrom.text.toString().toInt()
        val toAge = etTo.text.toString().toInt()
        try{
            val querySnapshot = personCollectionRef
                .whereGreaterThan("age",fromAge) // check age in between range or not
                .whereLessThan("age",toAge)
                .orderBy("age")
                .get()
                .await()  // store the document in personCollectionRef by get info

            val sb = StringBuilder()
            for (document in querySnapshot.documents){  // use for loop to store document one by one in persons objects to show in Ui layout
                val persons = document.toObject<Persons>() // we take default parameter -> person class for document
                sb.append("$persons\n")
            }
            withContext(Dispatchers.Main){
                PersonData.text = sb.toString()  // show the text in person data ide layout
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getNewPersonMap() : Map<String,Any>{
        val firstName = etNewFirstName.text.toString()
        val lastname = etNewLastName.text.toString()
        val age = etNewAge.text.toString()
        val map = mutableMapOf<String,Any>() // string is Mutable we can edit // Key is string and Any is Value

        if(firstName.isNotEmpty()){
            map["firstName"] = firstName // we put the First name in key value string
        }

        if(lastname.isNotEmpty()){   // if suppose last name is not entered so, it will go and check is empty or not then it will return the map
            map["lastName"] = lastname  // we put the last name in key value string
        }

        if(age.isNotEmpty()){
            map["age"] = age.toInt() // we put the age in key value string
        }
        return map
    }

    private fun updatePerson(persons: Persons,newPersonsMap: Map<String,Any>) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef // using Queries
            .whereEqualTo("firstName", persons.firstName)  // where we compare the string
            .whereEqualTo("lastName", persons.lastName)
            .whereEqualTo("age", persons.age)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()){

            for (document in personQuery){
                try {
                    personCollectionRef.document(document.id).set(
                            newPersonsMap,
                            SetOptions.merge() // Map is same as before
                    ).await()
                    } catch (e: Exception){
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
        else{
            withContext(Dispatchers.Main){

                Toast.makeText(this@MainActivity,"No person matched the query",Toast.LENGTH_SHORT).show()
            }
        }

    }



    private fun deletePerson(persons: Persons) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef // using Queries
            .whereEqualTo("firstName", persons.firstName)  // where we compare the string
            .whereEqualTo("lastName", persons.lastName)
            .whereEqualTo("age", persons.age)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {

            for (document in personQuery) {
                try {
                 personCollectionRef.document(document.id).delete()  // In this line we delete whole document id
//                     personCollectionRef.document(document.id).update(mapOf( // In this line we Only delete first name
//                         "firstName" to FieldValue.delete()))

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {

                Toast.makeText(this@MainActivity, "No person matched the query", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


//  BatchWrite -> A transaction consists of any number of get() operations followed by any number of write operations such as set(), update(),
//  or delete(). In the case of a concurrent edit, Cloud Firestore runs the entire transaction again. For example, if a transaction reads
//  documents and another client modifies any of those documents, Cloud Firestore retries the transaction.
//  This feature ensures that the transaction runs on up-to-date and consistent data.


    private fun changeName(
        personId: String,
        newFirstName: String,
        newLastName: String,

    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runBatch{ batch ->
                val personRef = personCollectionRef.document(personId)

                // we can update,delete , -->  Cloud Firestore retries the transaction. This feature ensures that the transaction runs on up-to-date and consistent data.
                batch.update(personRef,"firstName", newFirstName)
                batch.update(personRef,"lastName", newLastName)

            }   // you don't need to write commit because it already commit in braces

        }catch (e : Exception){
            withContext(Dispatchers.Main) {

                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun birthday(personId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runTransaction{ transaction ->


                val personRef = personCollectionRef.document(personId)
                val person = transaction.get(personRef)
                val newAge = person["age"] as Long + 1  // increment the age no.
                transaction.update(personRef,"age",newAge)
                null  // it represent our transaction was successful
            }.await()
        }
        catch (e : Exception){
            withContext(Dispatchers.Main) {

                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}


