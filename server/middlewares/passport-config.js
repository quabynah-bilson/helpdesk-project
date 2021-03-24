let passport = require("passport"),
  LocalStrategy = require("passport-local").Strategy,
  GoogleStrategy = require("passport-google-oauth2").Strategy;

// username + password strategy
let localStrategy = new LocalStrategy(async (username, password, done) => {
  console.log(username);
});

// google strategy
let googleStrategy = new GoogleStrategy(
  {
    clientID: process.env.GOOGLE_CLIENT_ID,
    clientSecret: process.env.GOOGLE_CLIENT_SECRET,
    callbackURL: "http://yourdomain:3000/auth/google/callback",
    passReqToCallback: true,
  },
  async (accessToken, refreshToken, profile, done) => {
    console.log(accessToken);
  }
);

passport.use(localStrategy);
passport.use(googleStrategy);

module.exports = {};
