const jwt = require('jsonwebtoken');
const AppError = require('../utils/AppError');
const { jwtSecret } = require('../config/env');

function authenticate(req, res, next) {
    const header = req.headers.authorization;

    if (!header || !header.startsWith('Bearer ')) {
        return next(new AppError('Missing or malformed Authorization header', 401));
    }

    const token = header.split(' ')[1];

    jwt.verify(token, jwtSecret, (err, payload) => {
        if (err) {
            return next(new AppError('Invalid or expired token', 401));
        }
        req.user = { id: payload.sub, role: payload.role };
        next();
    });
}

module.exports = authenticate;
