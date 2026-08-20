require('dotenv').config();

module.exports = {
    port: process.env.PORT || 3000,
    jwtSecret: process.env.JWT_SECRET,
    uploadDir: process.env.UPLOAD_DIR || 'uploads',
    maxUploadMb: Number(process.env.MAX_UPLOAD_MB || 5)
};
