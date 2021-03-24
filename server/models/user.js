let mongoose = require("mongoose");
let Schema = mongoose.Schema;

let userSchema = new Schema(
  {
    username: {
      type: String,
      required: true,
      min: 6,
      max: 255,
    },
    password: {
      type: String,
      required: true,
      min: 6,
      max: 255,
    },
    avatar: String,
    phoneNumber: String,
  },
  { collation: "users", autoIndex: true, timestamps: true }
);

// export User model
module.exports = mongoose.model("User", userSchema);
