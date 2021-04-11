const functions = require("firebase-functions");
const admin = require("firebase-admin");
// const mailer = require("nodemailer");
// require("dotenv").config();
// require("cors")({ origin: true });
admin.initializeApp();

/**
 * Here we're using Gmail to send
 */
// let transporter = mailer.createTransport({
//   service: "gmail",
//   auth: {
//     user: process.env.G_MAIL,
//     pass: process.env.G_PASSWORD,
//   },
// });

// send email for tickets
// https://cloud.google.com/functions/docs/env-var#cloud-console-ui
// exports.sendMail = functions.https.onRequest((req, res) => {
//   cors(req, res, () => {
//     // getting dest email by query string
//     const dest = req.query.dest;

//     const mailOptions = {
//       from: `Your Account Name <${process.env.G_MAIL}>`, // Something like: Jane Doe <janedoe@gmail.com>
//       to: dest,
//       subject: "I'M A PICKLE!!!", // email subject
//       html: `<p style="font-size: 16px;">Pickle Riiiiiiiiiiiiiiiick!!</p>
//                 <br />
//                 <img src="https://images.prod.meredith.com/product/fc8754735c8a9b4aebb786278e7265a5/1538025388228/l/rick-and-morty-pickle-rick-sticker" />
//             `, // email content in HTML
//     };

//     // returning result
//     return transporter.sendMail(mailOptions, (erro, info) => {
//       if (erro) {
//         return res.send(erro.toString());
//       }
//       return res.send("Sent");
//     });
//   });
// });

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

// todo -> deploy changes
// firebase deploy --only functions:sendTicketNotification && firebase dpeloy --only functions:sendFeedbackNotification
