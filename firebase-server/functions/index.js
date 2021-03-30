const functions = require("firebase-functions");
let admin = require("firebase-admin");
let mailer = require("nodemailer");
require("dotenv").config();
require("cors")({ origin: true });
admin.initializeApp();
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.test = functions.https.onRequest((request, response) => {
  functions.logger.info("Hello logs!", { structuredData: true });
  response.send("Hello from HelpDesk!");
});

/**
 * Here we're using Gmail to send
 */
let transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.G_MAIL,
    pass: process.env.G_PASSWORD,
  },
});

// send email for tickets
exports.sendMail = functions.https.onRequest((request, response) => {});
