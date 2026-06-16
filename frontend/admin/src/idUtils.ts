const CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

export function uuidToShortId(uuid: string): string {
    const hex = uuid.replace(/-/g, "");
    const msb = BigInt("0x" + hex.slice(0, 16));
    const lsb = BigInt("0x" + hex.slice(16));
    let v = msb ^ lsb;
    const chars = new Array(13);
    for (let i = 12; i >= 0; i--) {
        chars[i] = CROCKFORD[Number(v & 0x1fn)];
        v >>= 5n;
    }
    return chars.join("");
}
