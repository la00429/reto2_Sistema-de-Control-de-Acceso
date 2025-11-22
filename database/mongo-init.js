// Script de inicialización para MongoDB
// Otorga permisos al usuario root en la base de datos login_db

db = db.getSiblingDB('login_db');

// Crear usuario con permisos en login_db si no existe
try {
    db.createUser({
        user: 'admin',
        pwd: 'adminpassword',
        roles: [
            {
                role: 'readWrite',
                db: 'login_db'
            }
        ]
    });
    print('Usuario admin creado en login_db');
} catch (e) {
    // Si el usuario ya existe, otorgar permisos
    if (e.code === 51003) {
        db.grantRolesToUser('admin', [
            {
                role: 'readWrite',
                db: 'login_db'
            }
        ]);
        print('Permisos otorgados al usuario admin en login_db');
    } else {
        print('Error: ' + e);
    }
}

// También asegurar que el usuario root tenga permisos
db = db.getSiblingDB('admin');
db.grantRolesToUser('admin', [
    {
        role: 'readWrite',
        db: 'login_db'
    }
]);
print('Permisos otorgados al usuario root admin en login_db');




