const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// trigger notification when tickets are updated
exports.sendTicketNotification = functions.firestore
  .document("tickets/{id}")
  .onWrite(async (change, _context) => {
    let topic = "HelpDesk";

    // return if the document is deleted
    if (!change.after.exists)
      return Promise.reject(`no document found: ${change.before.id}`);

    let ticket = change.after.data(); // ticket

    // notification message
    let message = {
      data: {
        id: ticket._id,
        type: "tickets",
      },
      topic: topic,
    };

    return admin
      .messaging()
      .send(message)
      .then((response) => {
        // Response is a message ID string.
        console.log("Successfully sent message:", response);
      })
      .catch((error) => {
        console.log("Error sending message:", error);
      });
  });

// delete user account when removed from the database
exports.deleteUserAccount = functions.firestore
  .document("users/{id}")
  .onDelete(async (snapshot, context) => {
    let { token } = snapshot.data();
    if (!token) {
      return await admin.auth().deleteUser(context.params.id);
    }

    let payload = {
      data: {
        title: "Account deleted",
        body: "You are no longer a user of HelpDesk application. Please sign out and request a new account.",
        type: "deleted_accounts",
      },
    };
    return Promise.all([
      admin.messaging().sendToDevice(token, payload),
      admin.auth().deleteUser(context.params.id),
    ]);
  });

// todo -> deploy changes
// firebase login --reauth
// firebase deploy --only functions:sendTicketNotification && firebase dpeloy --only functions:sendFeedbackNotification
