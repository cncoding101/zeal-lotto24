const users = [
    {
        db: "demo",
        username: "demo_application",
        password: "demo_password",
    }
]

function userExists(user) {
    return db.getSiblingDB(user.db).getUser(user.username) != null;
}

users.forEach(function (user) {
    print(`Creating user ${user.username} on db ${user.db}`);
    if (!userExists(user)) {
        db.getSiblingDB(user.db).createUser({
            user: user.username,
            pwd: user.password,
            roles: [{role: "readWrite", db: user.db}]
        });
    }
})
