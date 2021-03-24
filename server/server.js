let express = require("express");
let app = express();
require("dotenv").config();
let mongoose = require("mongoose");

mongoose.connect(
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

let port = process.env.PORT || 8986;
app.listen(port, () => console.log(`server running on port -> ${port}`));
