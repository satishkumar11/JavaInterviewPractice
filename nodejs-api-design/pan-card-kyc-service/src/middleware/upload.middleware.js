const multer = require('multer');
const path = require('path');
const { randomUUID } = require('crypto');
const AppError = require('../utils/AppError');
const { uploadDir, maxUploadMb } = require('../config/env');

const ALLOWED_MIME_TYPES = new Set(['image/jpeg', 'image/png']);

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, uploadDir),
    filename: (req, file, cb) => {
        const ext = path.extname(file.originalname);
        cb(null, `${randomUUID()}${ext}`);
    }
});

function fileFilter(req, file, cb) {
    if (!ALLOWED_MIME_TYPES.has(file.mimetype)) {
        return cb(new AppError('Only JPEG or PNG images are accepted', 400));
    }
    cb(null, true);
}

const upload = multer({
    storage,
    fileFilter,
    limits: { fileSize: maxUploadMb * 1024 * 1024 }
});

module.exports = upload.single('panImage');
