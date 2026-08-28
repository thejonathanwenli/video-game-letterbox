export const API_BASE_URL = 'http://localhost:8080/api'

export async function readErrors(response) {
  try {
    const payload = await response.json()

    if (Array.isArray(payload)) {
      return payload
    }

    if (payload.message) {
      return [payload.message]
    }
  } catch {
    // The response did not contain JSON.
  }

  return ['Something went wrong. Please try again.']
}
