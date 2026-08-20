const AppError = require('../utils/AppError');
const documentService = require('../services/document.service');

const documentController = {
    uploadDocument(req, res) {
        if (!req.file) throw new AppError('panImage file is required', 400);
        const doc = documentService.uploadDocument(req.user, req.file);
        res.status(201).json({ id: doc.id, status: doc.status, createdAt: doc.createdAt });
    },

    getStatus(req, res) {
        const result = documentService.getStatus(req.user, req.params.id);
        res.status(200).json(result);
    },

    addMessage(req, res) {
        const message = documentService.addMessage(req.user, req.params.id, req.body.text);
        res.status(201).json(message);
    },

    updateMessage(req, res) {
        const message = documentService.updateMessage(
            req.user,
            req.params.id,
            req.params.messageId,
            req.body.text
        );
        res.status(200).json(message);
    },

    deleteMessage(req, res) {
        documentService.deleteMessage(req.user, req.params.id, req.params.messageId);
        res.status(204).send();
    }
};

module.exports = documentController;
