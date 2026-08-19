/**
 * The generated client derives its Accept type from the spec, which only
 * allows the wildcard header — the very value that makes HttpClient parse
 * bodies as Blobs instead of JSON. Widening the type lets the wrappers pin
 * application/json; the runtime string is passed through unchanged.
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const JSON_ACCEPT: any = { httpHeaderAccept: 'application/json' };
