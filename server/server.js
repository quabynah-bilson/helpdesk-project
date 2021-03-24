let express = require("express");
let app = express();
require("dotenv").config();
let passport = require("passport");
require("./middlewares/passport-config");

// connect to mongo database
require("mongoose").connect(
  process.env.MONGODB_CONNECTION_URL,
  { useUnifiedTopology: true, useNewUrlParser: true },
  (err) => {
    if (err) {
      console.error(err);
    } else {
      console.log("database connected");
    }
  }
);

// middlewares
app.use(require("cors")());
app.use(require("morgan")("combined"));
app.use(
  require("express-session")({
    resave: false,
    saveUninitialized: false,
    secret: require("uuid").v4(),
  })
);
app.use(passport.initialize());
app.use(passport.session());

// routes
app.use("/api/auth", require("./routes/auth"));

let port = process.env.PORT || 8986;
app.listen(port, () => console.log(`server running on port -> ${port}`));
