db.createUser(
    {
        user: "capi",
        pwd: "capi",
        roles: [
            {
                role: "readWrite",
                db: "capi"
            }
        ]
    }
)