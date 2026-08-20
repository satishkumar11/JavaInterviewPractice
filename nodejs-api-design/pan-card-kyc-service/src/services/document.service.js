const AppError = require('../utils/AppError');
const documentRepository = require('../repositories/document.repository');

function assertCanAccessDocument(doc, user) {
    if (doc.ownerId !== user.id && user.role !== 'admin' && user.role !== 'reviewer') {
        throw new AppError('You do not have access to this document', 403);
    }
}

const documentService = {
    uploadDocument(user, file) {
        return documentRepository.create({
            ownerId: user.id,
            imagePath: file.path,
            mimeType: file.mimetype,
            size: file.size
        });
    },

    getStatus(user, docId) {
        const doc = documentRepository.findById(docId);
        if (!doc) throw new AppError('Document not found', 404);
        assertCanAccessDocument(doc, user);
        return { id: doc.id, status: doc.status, updatedAt: doc.updatedAt };
    },

    addMessage(user, docId, text) {
        const doc = documentRepository.findById(docId);
        if (!doc) throw new AppError('Document not found', 404);
        assertCanAccessDocument(doc, user);
        if (!text || !text.trim()) throw new AppError('Message text is required', 400);
        return documentRepository.addMessage(docId, { authorId: user.id, text: text.trim() });
    },

    updateMessage(user, docId, messageId, text) {
        const doc = documentRepository.findById(docId);
        if (!doc) throw new AppError('Document not found', 404);

        const message = documentRepository.findMessage(docId, messageId);
        if (!message) throw new AppError('Message not found', 404);
        if (message.authorId !== user.id && user.role !== 'admin') {
            throw new AppError('You can only edit your own messages', 403);
        }
        if (!text || !text.trim()) throw new AppError('Message text is required', 400);

        return documentRepository.updateMessage(docId, messageId, text.trim());
    },

    deleteMessage(user, docId, messageId) {
        const doc = documentRepository.findById(docId);
        if (!doc) throw new AppError('Document not found', 404);

        const message = documentRepository.findMessage(docId, messageId);
        if (!message) throw new AppError('Message not found', 404);
        if (message.authorId !== user.id && user.role !== 'admin') {
            throw new AppError('You can only delete your own messages', 403);
        }

        documentRepository.deleteMessage(docId, messageId);
    }
};

module.exports = documentService;
