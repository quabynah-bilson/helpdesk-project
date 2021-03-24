let router = require("express").Router();
let passport = require("passport");
let User = require("../models/user");

router.get(
  "/google",
  passport.authenticate("google", { scope: ["email", "profile"] })
);

router.post(
  "/login",
  passport.authenticate("local", { failureRedirect: "/login" }),
  function (req, res) {
    return res.status(200).json({
      data: {
        username: "bilson@gmail.com",
        name: "Quabynah Bilson",
        avatar: "",
        phoneNumber: "",
      },
    });
  }
);

module.exports = router;
