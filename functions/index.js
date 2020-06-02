const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

exports.pushes = functions.firestore
.document('Notificacoes/{token}')
.onCreate((snap,context) => {
    const document = snap.data();
    console.log('document is',document);

    var registrationToken = context.params.token;
    var message = {
        data:{
            title: document.fromName,
            body: document.mensagem,
            sender: document.id
        },
        token: registrationToken
    }
    admin.messaging().send(message)
        .then((response) => {
            console.log('Successfull sent message:',response);
        })
    .catch((error) => {
        console.log('Error sent message',response);
    })
    
    admin.firestore()
    .collection("Notificacoes")
    .doc(registrationToken)
    .delete();

    return Promise.resolve(0);

});

