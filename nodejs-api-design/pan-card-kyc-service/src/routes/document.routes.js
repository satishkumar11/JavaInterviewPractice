const express = require('express');
const authenticate = require('../middleware/auth.middleware');
const uploadPanImage = require('../middleware/upload.middleware');
const documentController = require('../controllers/document.controller');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.use(authenticate);

// 1) Upload a PAN card image
router.post('/', uploadPanImage, asyncHandler(documentController.uploadDocument));

// 2) Check verification status
router.get('/:id/status', asyncHandler(documentController.getStatus));

// 3) Add a reviewer message
router.post('/:id/messages', asyncHandler(documentController.addMessage));

// 4) Delete a message
router.delete('/:id/messages/:messageId', asyncHandler(documentController.deleteMessage));

// 5) Update a message
router.patch('/:id/messages/:messageId', asyncHandler(documentController.updateMessage));

module.exports = router;
