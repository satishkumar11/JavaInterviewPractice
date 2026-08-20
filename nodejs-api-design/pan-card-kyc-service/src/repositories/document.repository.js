const { randomUUID } = require('crypto');

const documents = new Map();

const documentRepository = {
    create({ ownerId, imagePath, mimeType, size }) {
        const now = new Date().toISOString();
        const doc = {
            id: randomUUID(),
            ownerId,
            imagePath,
            mimeType,
            size,
            status: 'PENDING',
            messages: [],
            createdAt: now,
            updatedAt: now
        };
        documents.set(doc.id, doc);
        return doc;
    },

    findById(id) {
        return documents.get(id) || null;
    },

    findMessage(docId, messageId) {
        const doc = documents.get(docId);
        if (!doc) return null;
        return doc.messages.find((m) => m.id === messageId) || null;
    },

    addMessage(docId, { authorId, text }) {
        const doc = documents.get(docId);
        if (!doc) return null;
        const now = new Date().toISOString();
        const message = { id: randomUUID(), authorId, text, createdAt: now, updatedAt: now };
        doc.messages.push(message);
        doc.updatedAt = now;
        return message;
    },

    updateMessage(docId, messageId, text) {
        const message = this.findMessage(docId, messageId);
        if (!message) return null;
        message.text = text;
        message.updatedAt = new Date().toISOString();
        return message;
    },

    deleteMessage(docId, messageId) {
        const doc = documents.get(docId);
        if (!doc) return false;
        const index = doc.messages.findIndex((m) => m.id === messageId);
        if (index === -1) return false;
        doc.messages.splice(index, 1);
        doc.updatedAt = new Date().toISOString();
        return true;
    }
};

module.exports = documentRepository;
