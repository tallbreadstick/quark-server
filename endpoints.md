# About

Quark is a web-based learning platform for STEM education through hands-on, interactive programming activities. Users can create educator or student accounts. Educators can create, fork, and share courses, while students can enroll in courses and complete activities.

# MySQL Entities

*types with a `?` appended indicate nullable types.* \
*types with a `$` prepended indicate unique values. All primary keys are unique by default.* \
*types with a `&` prepended indicate enforced cascade deletion if the object of the foreign key is deleted.* \
*types with a `?` prepended indicate enforced cascade set null if the object of the foreign key is deleted. This type is also nullable.*


> @User

    id INT PK SERIAL
    username VARCHAR(32)
    email $VARCHAR(100)
    password VARCHAR(128)
    user_type ENUM("EDUCATOR", "STUDENT")
    created_at DATETIME

> @Profile

    user_id &INT PK => @User(id)
    image LONGBLOB?
    bio VARCHAR(255)?
    updated_at DATETIME

> @Course

    id INT PK SERIAL
    name VARCHAR(255)
    description VARCHAR(255)?
    introduction TEXT?
    version INT
    origin ?INT => @Course(id)
    owner &INT => @User(id)
    created_at DATETIME
    updated_at DATETIME

> @Tag

    id INT PK SERIAL
    name VARCHAR(50)

> @CourseTag

    course_id &INT PK => @Course(id)
    tag_id &INT PK => @Tag(id)

> @Chapter

    id INT PK SERIAL
    name VARCHAR(255)
    idx INT
    description VARCHAR(255)?
    icon VARCHAR(100)?
    course_id &INT => @Course(id)

> @Activity

    id INT PK SERIAL
    name VARCHAR(255)
    idx INT
    ruleset TEXT
    description VARCHAR(255)?
    icon VARCHAR(100)?
    finish_message TEXT?
    version INT
    chapter_id &INT => @Chapter(id)

> @Lesson

    id INT PK SERIAL
    name VARCHAR(255)
    idx INT
    description VARCHAR(255)?
    icon VARCHAR(100)?
    finish_message TEXT?
    version INT
    chapter_id &INT => @Chapter(id)

> @Section

    id INT PK SERIAL
    idx INT
    content MEDIUMTEXT?
    activity_id &INT => @Activity(id)

> @Page

    id INT PK SERIAL
    idx INT
    content MEDIUMTEXT?
    lesson_id &INT => @Lesson(id)

> @CourseShared

    user_id &INT PK => @User(id)
    course_id &INT PK => @Course(id)

> @CourseProgress

    user_id &INT PK => @User(id)
    course_id &INT PK => @Course(id)
    version INT

> @ChapterProgress

    user_id &INT PK => @User(id)
    chapter_id &INT PK => @Chapter(id)
    version INT

> @ActivityProgress

    user_id &INT PK => @User(id)
    activity_id &INT PK => @Activity(id)
    version INT

# REST API Endpoints

## @User

### Register

Handles the creation of a user account.

`POST` => `/api/auth/register`

`Content-Type: application/json`

    /Json/
    {
        "username": <username>,
        "email": <email>,
        "password": <password>,
        "user_type": <"EDUCATOR" | "STUDENT">
    }

`username`:
- must be between 3 and 32 characters long
- must consist of numbers, letters, `_`, or `.` only
- cannot contain whitespace
- must be unique

`email`:
- must be valid email format (i.e. LIKE '%@%.%')
- must be unique
- must be no more than 100 characters

`password`:
- must be at least 8 characters long
- must contain at least 1 letter, number, and special character
- use argon2 to hash passwords

Success:

> `200 OK`

Failure:

> `400 BAD REQUEST` if fields supplied are invalid \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Login

Handles validating user credentials and returning a json web token.

`POST` => `/api/auth/login`

`Content-Type: application/json`

    /Json/
    {
        "identifier": <email | username>,
        "password": <password>
    }

`identifier`:
- can be an existing username or email

`password`:
- must match the password of the user with the given `username` or `email`
- use argon2 to resolve passwords

Success:

> `200 OK`

    /Json/
    {
        "jwt": <token>,
        "username": <username>,
        "email": <email>
    }

*The json web token must be formed out of the following claims: `user_id`, `expiration`, `user_type`*

Failure:

> `401 UNAUTHORIZED` if credentials supplied are incorrect \
> `500 INTERNALE SERVER ERROR` if some database operation fails

***

### Fetch Users By Identifier

Fetches a list of 10 users most closely matching the given username or email identifier, used for looking up users in the "share with other users" features.

`GET` => `/api/users`

`Content-Type: application/x-www-form-urlencoded` \
`Authorization: Bearer <token>`

    /UrlEncoded/
    identifier=<identifier>

`token`:
- must be a valid token returned from a login request and not expired

`identifier`
- must be either a username or an email
- assume email if `LIKE %@%.@` is satisfied, else username

Success:

> `200 OK`

    /Json/
    [
        {
            "id": <user_id>,
            "username": <username>,
            "email": <email>
        }
    ]

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired \
> `400 BAD REQUEST` if the query format is invalid \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

## @Profile

### Upload Profile Picture

Handles uploading a profile picture and assigning it as the profile picture to the user of the `user_id` parsed from the bearer token.

`POST` => `/api/profile`

`Content-Type: multipart/form-data` \
`Authorization: Bearer <token>`

    /FormData/
    image: <image>

`token`:
- must be a valid token returned from a login request and not expired

`image`:
- size must not exceed 2 megabytes

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired \
> `404 NOT FOUND` if the id parsed does not belong to an existing user \
> `413 CONTENT TOO LARGE` if file size exceeds limit \
> `400 BAD REQUEST` if image is invalid for any other reason \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fetch Profile Picture By Id

Handles fetching a profile picture from a user and returns it as base64 encoded.

`GET` => `/api/profile/:user_id`

`NO HEADERS REQUIRED`

`NO BODY REQUIRED`

`user_id`:
- must be a valid `user_id` from a user entity

Success:

> `200 OK`

    /Text/
    data:<base64_img>;

Failure:

> `204 NO CONTENT` if the profile of supplied `user_id` contains no image \
> `404 NOT FOUND` if the supplied `user_id` is does not exist \
> `500 INTERAL SERVER ERROR` if some database operation fails

***

### Clear Profile Picture By Id

Handles clearing a user's profile picture.

`DELETE` => `/api/profile`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired \
> `404 NOT FOUND` if the id parsed does not belong to an existing user \
> `400 BAD REQUEST` if image is invalid for any other reason \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Update Bio By Id

Handles setting a user's bio.

`POST` => `/api/profile/bio`

`Content-Type: text/plain` \
`Authorization: Bearer <token>`

    /Text/
    <bio>

`bio`:
- bio must not exceed 255 characters

`token`:
- must be a valid token returned from a login and not expired

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired \
> `404 NOT FOUND` if the id parsed does not belong to an existing user \
> `500 INTERNAL SERVER ERROR` if some database operation fails

*clearing the bio simply uses this endpoint but passes empty text*

***

### Fetch Bio By Id

Handles retrieving a user's bio.

`GET` => `/api/profile/bio/:user_id`

`NO HEADERS REQUIRED`

`NO BODY REQUIRED`

`user_id`:
- must be a valid `user_id` from a user entity

Success:

> `200 OK`

    /Text/
    <bio>

Failure:

> `404 NOT FOUND` if the supplied `user_id` is does not exist \
> `500 INTERAL SERVER ERROR` if some database operation fails

***

## @Course

### Create Course

Handles creation of course and definition of course metadata by a user who is an educator.

`POST` => `/api/courses`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "name": <course_name>,
        "description": <course_description>,
        "introduction: <introduction>,
        "forkable": <"true" | "false">,
        "visibility": <"PUBLIC" | "PRIVATE" | "UNLISTED">,
        "tags": [ <course_tags> ]
    }

`token`:
- must be a valid token returned from a login and not expired

`course_name`:
- must be between 10 and 255 characters

`course_description`:
- can be empty
- must not exceed 255 characters

`introduction`:
- can be empty
- must fit in a `TEXT` type mysql field

`forkable`:
- indicates whether you allow other users to create a copy of your course or not

`visibility`:
- indicates whether the course is publicly visible, private to a set of users, or unlisted to anyone without a course link

`course_tags`:
- comma separated tags
- backend must validate these tags to be existing
- cannot assign more than 3 tags

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired \
> `404 NOT FOUND` if the id parsed does not belong to an existing user \
> `400 BAD REQUEST` if the request is invalid for any other reason \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fork Course By Id

Creates a personal copy of a course that has been shared with an educator user or public. This is a deep copy which also duplicates all subcomponents such as chapters, lessons, activities, sections, and pages.

`POST` => `api/courses/:course_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "name": <course_name>,
        "description": <course_description>,
        "introduction: <introduction>,
        "forkable": <"true" | "false">,
        "visibility": <"PUBLIC" | "PRIVATE" | "UNLISTED">,
        "tags": [ <course_tags> ]
    }

`token`:
- must be a valid token returned from a login and not expired

`course_name`:
- must be between 10 and 255 characters

`course_description`:
- can be empty
- must not exceed 255 characters

`introduction`:
- can be empty
- must fit in a `TEXT` type mysql field

`forkable`:
- indicates whether you allow other users to create a copy of your course or not

`visibility`:
- indicates whether the course is publicly visible, private to a set of users, or unlisted to anyone without a course link

`course_tags`:
- comma separated tags
- backend must validate these tags to be existing
- cannot assign more than 3 tags

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired \
> `404 NOT FOUND` if the id parsed does not belong to an existing user \
> `400 BAD REQUEST` if the request is invalid for any other reason \
> `500 INTERNAL SERVER ERROR` if some database operation fails

### Edit Course By Id

Handles editing course metadata by `course_id` and validated by a token.

`PUT` => `/api/courses/:course_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "name": <course_name>,
        "description": <course_description>,
        "introduction: <introduction>,
        "forkable": <"true" | "false">,
        "visibility": <"PUBLIC" | "PRIVATE" | "UNLISTED">,
        "tags": [ <course_tags> ]
    }

`token`:
- must be a valid token returned from a login and not expired

`course_id`:
- must reference an existing course owned by the user parsed from the token

`course_name`:
- must be between 10 and 255 characters

`course_description`:
- can be empty
- must not exceed 255 characters

`introduction`:
- can be empty
- must fit in a `TEXT` type mysql field

`forkable`:
- indicates whether you allow other users to create a copy of your course or not

`visibility`:
- indicates whether the course is publicly visible, private to a set of users, or unlisted to anyone without a course link

`course_tags`:
- comma separated tags
- backend must validate these tags to be existing
- cannot assign more than 3 tags

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user does not own the course \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the course id does not exist \
> `400 BAD REQUEST` if the request is invalid for any other reason \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Delete Course By Id

Handles deletion of a course, cascading to all subcomponents, by a given `course_id`.

`DELETE` => `/api/courses/:course_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login and not expired

`course_id`:
- must reference an existing course owned by the user parsed from the token

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user does not own the course \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the course id does not exist \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fetch Courses By Filter

Fetches all courses that satisfy a set of filters. Retrieves only courses that are publicly listed.

`GET` => `/api/courses`

`Content-Type: application/x-www-form-urlencoded` \
`Authorization: Bearer <token>`

    /UrlEncoded/
    my_courses=<"true" | "false" | "either">
    shared_with_me=<"true" | "false" | "either">
    forkable-<"true" | "false" | "either">
    tags=<course_tags>
    sort_by=<"name" | "date_created" | "students_enrolled">
    order=<"ascending" | "descending">
    search=<pattern>

`my_courses`:
- filters courses enrolled in by the user as a student or owns as an educator, if set to true
- ignore this parameter if not supplied, default to false

`shared_with_me`:
- filters courses that are shared with the user, if set to true
- ignore this parameter if not supplied, default to false

`forkable`:
- indicates other users are allowed to create a copy of the course or not

`tags`:
- comma separated string of tags
- ignore this parameter if not supplied

`sort_by`:
- a key on which to sort the fetched courses, either by `name`, `date_created`, or `students_enrolled`
- ignore this parameter if not supplied, use default order as fetched

`order`:
- the order in which to arrange the fetched courses, either `ascending` or `descending` order
- ignore this parameter if not supplied or if `sort_by` is not supplied

`search`:
- a string pattern in which to match to course names or descriptions
- ignore this parameter if not supplied

Success:

> `200 OK`

    /Json/
    [
        {
            "id": <course_id>,
            "name": <course_name>,
            "description": <course_description>,
            "introduction": <introduction>,
            "tags": [ <course_tags> ]
        }
    ]

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired \
> `404 NOT FOUND` if the id parsed from the token does not belong to an existing user \
> `400 BAD REQUEST` if the query format is invalid \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fetch Course With Chapters By Id

Fetches a specific course by id, if the user owns the course as an educator, is enrolled in it as a student, or if the course has been shared to them privately. Also returns the chapter structure.

`GET` => `/api/courses/:course_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login and not expired

`course_id`:
- must reference an existing course owned by the user parsed from the token

Success:

> `200 OK`

    /Json/
    {
        "id": <course_id>,
        "name": <course_name>,
        "description": <course_description>,
        "introduction": <introduction>,
        "forkable": <"true" | "false">
        "tags": [ <course_tags> ],
        "chapters": [
            {
                "id": <chapter_id>
                "idx": <chapter_index>,
                "name": <chapter_name>,
                "description": <chapter_description>,
                "icon": <chapter_icon>
            }
        ]
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user does not own the course \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the course id does not exist \
> `400 BAD REQUEST` if the request is invalid for any other reason \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Share Course

Shares a course that the user owns as an educator to another user by id.

`POST` => `/api/courses/share`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "course_id": <course_id>,
        "user_id": <user_id>
    }

`token`:
- must be a valid token returned from a login request and not expired

`course_id`
- must reference an existing course owned by the user parsed from the token

`user_id`
- must reference an existing user to share the course with

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user does not own the course \
> `404 NOT FOUND` if the id parsed does not belong to an existing user, the course id does not exist, or the user being shared with does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Enroll In Course

Handles enrollment of a student user in a course. Creates a CourseProgress entry tracking the student's enrollment and the course version at the time of enrollment.

`POST` => `/api/courses/:course_id/enroll`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired
- user parsed from token must be of type "STUDENT"

`course_id`:
- must reference an existing course
- course must be PUBLIC, shared with the user, or the user must have access to it

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user is not a STUDENT \
> `403 FORBIDDEN` if the course is PRIVATE and not shared with the user \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the course id does not exist \
> `409 CONFLICT` if the user is already enrolled in the course \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Unenroll From Course

Handles unenrollment of a student user from a course. Deletes the CourseProgress entry and cascades to all related progress entries.

`DELETE` => `/api/courses/:course_id/enroll`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired
- user parsed from token must be of type "STUDENT"

`course_id`:
- must reference an existing course that the user is enrolled in

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user is not a STUDENT \
> `404 NOT FOUND` if the id parsed does not belong to an existing user, the course id does not exist, or the user is not enrolled in the course \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

## @Chapter

### Add Chapter To Course

Adds a chapter to a course

`POST` => `/api/course/:course_id/chapter`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "name": <chapter_name>,
        "description": <chapter_description>,
        "icon": <chapter_icon>
    }

`token`:
- must be a valid token returned from a login request and not expired

`course_id`
- must reference an existing course owned by the user parsed from the token

`chapter_name`:
- must be between 10 and 255 characters

`chapter_description`:
- can be empty
- must not exceed 255 characters

`chapter_icon`:
- nullable identifier for fontawesome icon

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user does not own the course \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the course id does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Reorder Chapters By Course

Receives an ordered list of course ids and serially reassigns to them their `idx` attributes to match the given order.

`PATCH` => `/api/course/:course_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    [
        <chapter_ids>
    ]

`token`:
- must be a valid token returned from a login request and not expired

`course_id`
- must reference an existing course owned by the user parsed from the token

`chapter_ids`:
- an ordered list of ids referencing chapters

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a chapter under a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or any chapter supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Edit Chapter By Id

Edits the metadata of a chapter in a course

`PUT` => `/api/chapter/:chapter_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "name": <chapter_name>,
        "description": <chapter_description>,
        "icon": <chapter_icon>
    }

`token`:
- must be a valid token returned from a login request and not expired

`chapter_id`
- must reference an existing chapter in a course owned by the user parsed from the token

`chapter_name`:
- must be between 10 and 255 characters

`chapter_description`:
- can be empty
- must not exceed 255 characters

`chapter_icon`:
- nullable identifier for fontawesome icon

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user does not own the course containing the chapter \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the chapter does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Delete Chapter By Id

Deletes a chapter from within a course, updates the `idx` ordering of the chapters in the same course. Cascades deletion to all chapter subcomponents.

`DELETE` => `/api/chapter/:chapter_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`chapter_id`
- must reference an existing chapter in a course owned by the user parsed from the token

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user does not own the course containing the chapter \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the chapter does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fetch Chapter With Items By Id

Fetches a chapter from a course along with its lessons and activites, in order.

`GET` => `/api/chapter/:chapter_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`chapter_id`
- must reference an existing chapter in a course owned by the user parsed from the token

Success:

> `200 OK`

    /Json/
    {
        "id": <chapter_id>,
        "idx": <chapter_index>,
        "name": <chapter_name>,
        "description": <chapter_description>,
        "icon": <chapter_icon>,
        "items": [
            {
                "id": <item_id>,
                "item_type": <"ACTIVITY" | "LESSON">,
                "idx": <item_index>,
                "ruleset": <ruleset? ONLY FOR item_type=ACTIVITY>
                "description": <item_description>,
                "icon": <item_icon>,
                "finish_message": <finish_message>
            }
        ]
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user does not own the course containing the chapter \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the chapter does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Reorder Chapter Items By Id

Reorders the items in a chapter by receiving an ordered list of objects indicating the activity or lesson id as well as item type, then reassigning their `idx` attribute based on the given order.

`PATCH` => `/api/chapter/:chapter_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    [
        {
            "id": <item_id>,
            "item_type": <"ACTIVITY" | "LESSON">
        }
    ]

`token`:
- must be a valid token returned from a login request and not expired

`chapter_id`
- must reference an existing chapter in a course owned by the user parsed from the token

`item_id`:
- the id referencing either an activity or a lesson

Success:

> `200 OK`

Failure:

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a chapter under a course they do not own, or if any item id belongs to a chapter under a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or any chapter or item supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

## @Lessons

### Add Lesson To Chapter

Adds a lesson to a chapter.

`POST` => `/api/chapter/:chapter_id/lesson`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "name": <lesson_name>,
        "description": <lesson_description>,
        "icon": <lesson_icon>,
        "finish_message": <finish_message>
    }

`token`:
- must be a valid token returned from a login request and not expired

`chapter_id`
- must reference an existing chapter in a course owned by the user parsed from the token

`lesson_name`:
- must be between 10 and 255 characters

`lesson_description`:
- can be empty
- must not exceed 255 characters

`lesson_icon`:
- optional identifier for fontawesome icon

`finish_message`
- message for user upon completing a lesson

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a chapter under a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or any chapter or item supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Edit Lesson By Id

Edits the metadata of a lesson in a chapter.

`PUT` => `/api/lesson/:lesson_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "name": <lesson_name>,
        "description": <lesson_description>,
        "icon": <lesson_icon>,
        "finish_message": <finish_message>
    }

`token`:
- must be a valid token returned from a login request and not expired

`lesson_id`
- must reference an existing lesson in a chapter from a course owned by the user parsed from the token

`lesson_name`:
- must be between 10 and 255 characters

`lesson_description`:
- can be empty
- must not exceed 255 characters

`lesson_icon`:
- optional identifier for fontawesome icon

`finish_message`
- message for user upon completing a lesson

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a chapter under a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or any chapter or item supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Delete Lesson By Id

Deletes a lesson referenced by an id, updates the `idx` ordering of lessons and activities in the same chapter. Deletions cascade to all subcomponents.

`DELETE` => `/api/lesson/:lesson_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`lesson_id`
- must reference an existing lesson in a chapter from a course owned by the user parsed from the token

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a lesson under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the lesson supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fetch Lesson Pages By Id

Fetches the metadata of a lesson and its pages by id.

`GET` => `/api/lesson/:lesson_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`lesson_id`
- must reference an existing lesson in a chapter from a course owned by the user parsed from the token

Success:

> `200 OK`

    /Json/
    {
        "id": <lesson_id>,
        "idx": <item_index>,
        "name": <lesson_name>,
        "description": <lesson_description>,
        "icon": <lesson_icon>,
        "finish_message": <finish_message>,
        "pages": [
            {
                "id": <page_id>,
                "idx": <page_index>
            }
        ]
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a lesson under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the lesson supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

## @Activities

### Add Activity To Chapter

Adds an activity to a chapter.

`POST` => `/api/chapter/:chapter_id/activity`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "name": <activity_name>,
        "description": <activity_description>,
        "icon": <activity_icon>,
        "ruleset": <ruleset>,
        "finish_message": <finish_message>
    }

`token`:
- must be a valid token returned from a login request and not expired

`chapter_id`
- must reference an existing chapter in a course owned by the user parsed from the token

`activity_name`:
- must be between 10 and 255 characters

`activity_description`:
- can be empty
- must not exceed 255 characters

`activity_icon`:
- optional identifier for fontawesome icon

`ruleset`:
- a json string of rules and configurations for the activities
- `sequential: true` indicates that sections of an activity must be done in order
- `shuffled: true` indicates that sections are randomly shuffled with each attempt
- `show_score: true` indicates that the user can see their score at the end of the activity, otherwise only have the course owner see the score
- `deadline` accepts a formatted date string that indicates the activity deadline, otherwise no deadline if null
- `time_limit` accepts some numeric value `n` where students get `n` minutes to complete the activity, otherwise no time limit if null

`finish_message`
- message for user upon completing a lesson

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a chapter under a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or any chapter or item supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Edit Activity By Id

Edits activity metadata and rulesets by id.

`PATCH` => `/api/activity/:activity_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "name": <activity_name>,
        "description": <activity_description>,
        "icon": <activity_icon>,
        "ruleset": <ruleset>,
        "finish_message": <finish_message>
    }

`token`:
- must be a valid token returned from a login request and not expired

`activity_id`
- must reference an existing lesson in a chapter from a course owned by the user parsed from the token

`activity_name`:
- must be between 10 and 255 characters

`activity_description`:
- can be empty
- must not exceed 255 characters

`activity_icon`:
- optional identifier for fontawesome icon

`ruleset`:
- a json string of rules and configurations for the activities
- `sequential: true` indicates that sections of an activity must be done in order
- `shuffled: true` indicates that sections are randomly shuffled with each attempt
- `show_score: true` indicates that the user can see their score at the end of the activity, otherwise only have the course owner see the score
- `deadline` accepts a formatted date string that indicates the activity deadline, otherwise no deadline if null
- `time_limit` accepts some numeric value `n` where students get `n` minutes to complete the activity, otherwise no time limit if null

`finish_message`
- message for user upon completing a lesson

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a chapter under a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or any chapter or item supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Delete Activity By Id

Deletes an activity by its id, and reordering the `idx` fields of the lessons and activities under the same chapter according to the change. Deletions cascade to all subcomponents.

`DELETE` => `/api/activity/:activity_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`activity_id`
- must reference an existing lesson in a chapter from a course owned by the user parsed from the token

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a chapter under a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or any chapter or item supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fetch Activity Section By Id

Fetches the metadata of an activity and its sections by id.

`GET` => `/api/activity/:activity_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`activity_id`
- must reference an existing activity in a chapter from a course owned by the user parsed from the token

Success:

> `200 OK`

    /Json/
    {
        "id": <activity_id>,
        "idx": <item_index>,
        "name": <activity_name>,
        "description": <activity_description>,
        "icon": <activity_icon>,
        "finish_message": <finish_message>,
        "sections": [
            {
                "id": <section_id>,
                "idx": <section_index>
            }
        ]
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies an activity under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the activity supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

## @Pages

### Create Page

Creates a page within a lesson.

`POST` => `/api/lesson/:lesson_id/page`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "renderer": <"markdown" | "latex">,
        "content": <content>
    }

`token`:
- must be a valid token returned from a login request and not expired

`lesson_id`
- must reference an existing lesson in a chapter from a course owned by the user parsed from the token

`content`:
- full markdown or LaTeX content of a given page

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a lesson under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the lesson supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Reorder Pages By Lesson Id

Reorders the `idx` fields of all the pages in a lesson given an ordered list of page ids.

`PATCH` => `/api/lesson/:lesson_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    [
        <page_ids>
    ]

`token`:
- must be a valid token returned from a login request and not expired

`lesson_id`
- must reference an existing lesson in a chapter from a course owned by the user parsed from the token

`page_ids`:
- an ordered list of ids referencing all the pages within a lesson

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a lesson under a chapter from a course they do not own, or if any page id does not belong to an owned lesson \
> `404 NOT FOUND` if the id parsed does not belong to an existing user, or the lesson supplied does not exist, or any of the page ids do not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Edit Page By Id

Edits the metadata and contents of a page by id.

`PUT` => `/api/page/:page_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "renderer": <"markdown" | "latex">,
        "content": <content>
    }

`token`:
- must be a valid token returned from a login request and not expired

`page_id`
- must reference an existing page from a lesson in a chapter from a course owned by the user parsed from the token

`content`:
- full markdown or LaTeX content of a given page

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a page from a lesson under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the page supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Delete Page By Id

Deletes a page, reorders the `idx` fields of all sibling pages.

`DELETE` => `/api/page/:page_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`page_id`
- must reference an existing page from a lesson in a chapter from a course owned by the user parsed from the token

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a page from a lesson under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the page supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fetch Page Contents By Id

Fetches the markdown contents of a page.

`GET` => `/api/page/:page_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`page_id`
- must reference an existing page from a lesson in a chapter from a course owned by the user parsed from the token

Success:

> `200 OK`

    /Json/
    {
        "renderer": <"markdown" | "latex">,
        "content": <content>
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a page from a lesson under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the page supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

## @Sections

### Create Section

Creates a section within an activity.

`POST` => `/api/activity/:activity_id/section`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "section_type": <"mcq" | "code">,
        "mcq": {
            "instructions": <mcq_instructions>,
            "questions": [
                {
                    "question": <question>,
                    "points": <points>,
                    "correct": <correct_choice>
                    "choices": [
                        <choices>
                    ]
                }
            ]
        },
        "code": {
            "renderer": <"markdown" | "latex">
            "instructions": <code_instructions>,
            "default_code": <default_code>,
            "sources": [
                <source_files>
            ]
            "test_cases": [
                {
                    "expected": <expected>,
                    "driver": <test_driver_code>
                    "points": <points>,
                    "hidden": <"true" | "false">
                }
            ]
        }
    }

`token`:
- must be a valid token returned from a login request and not expired

`activity_id`
- must reference an existing activity in a chapter from a course owned by the user parsed from the token

`section_type`:
- indicates whether the section is for multiple choice questions or code

`mcq_instructions`:
- plain text dictating the instructions for the multiple choice section

`renderer`:
- indicates whether the code instructions should be rendered with markdown or LaTeX

`code_instructions`:
- markdown or LaTeX text dictating the coding problem instructions

`points`:
- points awarded for getting an item or test case correct

`question`:
- individual multiple choice question

`correct_choice`:
- the correct answer out of the choices in a multiple choice question

`choices`:
- the choices of a multiple choice question

`default_code`:
- the default template code given for solving the coding problem

`source_files`:
- additional contents from source code files to given context to the user's file

`expected`:
- written details of an expected test case

`driver`:
- driver code for testing a given test case

`hidden`:
- determines whether the expected details of a test case are hidden or not

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies an activity under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the activity supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Reorder Sections By Activity Id

Reorders the `idx` fields of all the sections in an activity given an ordered list of section ids.

`PATCH` => `/api/activity/:activity_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    [
        <section_ids>
    ]

`token`:
- must be a valid token returned from a login request and not expired

`activity_id`
- must reference an existing activity in a chapter from a course owned by the user parsed from the token

`section_ids`:
- an ordered list of ids referencing all the sections within an activity

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies an activity under a chapter from a course they do not own, or if any section id does not belong to an owned activity \
> `404 NOT FOUND` if the id parsed does not belong to an existing user, or the activity supplied does not exist, or any of the section ids do not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Edit Section By Id

Edits the metadata and contents of a section within an activity.

`PUT` => `/api/section/:section_id`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "section_type": <"mcq" | "code">,
        "mcq": {
            "instructions": <mcq_instructions>,
            "questions": [
                {
                    "question": <question>,
                    "points": <points>,
                    "correct": <correct_choice>
                    "choices": [
                        <choices>
                    ]
                }
            ]
        },
        "code": {
            "renderer": <"markdown" | "latex">,
            "instructions": <code_instructions>,
            "default_code": <default_code>,
            "sources": [
                <source_files>
            ]
            "test_cases": [
                {
                    "expected": <expected>,
                    "driver": <test_driver_code>
                    "points": <points>,
                    "hidden": <"true" | "false">
                }
            ]
        }
    }

`token`:
- must be a valid token returned from a login request and not expired

`section_id`
- must reference an existing section from an activity in a chapter from a course owned by the user parsed from the token

`section_type`:
- indicates whether the section is for multiple choice questions or code

`mcq_instructions`:
- plain text dictating the instructions for the multiple choice section

`renderer`:
- indicates whether the code instructions should be rendered with markdown or LaTeX

`code_instructions`:
- markdown or LaTeX text dictating the coding problem instructions

`points`:
- points awarded for getting an item or test case correct

`question`:
- individual multiple choice question

`correct_choice`:
- the correct answer out of the choices in a multiple choice question

`choices`:
- the choices of a multiple choice question

`default_code`:
- the default template code given for solving the coding problem

`source_files`:
- additional contents from source code files to given context to the user's file

`expected`:
- written details of an expected test case

`driver`:
- driver code for testing a given test case

`hidden`:
- determines whether the expected details of a test case are hidden or not

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a section from an activity under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the section supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Delete Section By Id

Deletes a section of an activity, reorders the `idx` fields of all sibling sections.

`DELETE` => `/api/section/:section_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`section_id`
- must reference an existing section from an activity in a chapter from a course owned by the user parsed from the token

Success:

> `200 OK`

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a section from an activity under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the section supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fetch Section Contents By Id

Fetches the json contents of an activity section.

`GET` => `/api/section/:section_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`section_id`
- must reference an existing section from an activity in a chapter from a course owned by the user parsed from the token

Success:

> `200 OK`

    /Json/
    {
        "id": <section_id>,
        "section_type": <"mcq" | "code">,
        "mcq": {
            "instructions": <mcq_instructions>,
            "questions": [
                {
                    "question": <question>,
                    "points": <points>,
                    "correct": <correct_choice>
                    "choices": [
                        <choices>
                    ]
                }
            ]
        },
        "code": {
            "renderer": <"markdown" | "latex">,
            "instructions": <code_instructions>,
            "default_code": <default_code>,
            "sources": [
                <source_files>
            ]
            "test_cases": [
                {
                    "expected": <expected>,
                    "driver": <test_driver_code>
                    "points": <points>,
                    "hidden": <"true" | "false">
                }
            ]
        }
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user supplies a section from an activity under a chapter from a course they do not own \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the section supplied does not exist \
> `400 BAD REQUEST` if the request body contains invalid format \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

## @ActivityProgress

### Fetch Attempts

Fetches all attempts made by the user for a specific activity, including attempt metadata, scores, and submission status.

`GET` => `/api/activity/:activity_id/attempts`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`activity_id`:
- must reference an existing activity
- user must be enrolled in the course containing the activity, or own the course as an educator

Success:

> `200 OK`

    /Json/
    [
        {
            "attempt_id": <attempt_id>,
            "version": <activity_version>,
            "started_at": <started_at_datetime>,
            "submitted_at": <submitted_at_datetime?>,
            "status": <"IN_PROGRESS" | "SUBMITTED">,
            "score": <score?>,
            "max_score": <max_score>,
            "time_remaining": <time_remaining_seconds?>
        }
    ]

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired \
> `403 FORBIDDEN` if the user is not enrolled in the course containing the activity and does not own the course \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the activity does not exist \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Start Or Continue Attempt

Starts a new attempt or returns the current in-progress attempt for an activity. If an in-progress attempt exists, returns that attempt. Otherwise, creates a new attempt and initializes it based on the activity's ruleset (shuffled sections, sequential requirements, etc.).

`POST` => `/api/activity/:activity_id/attempt`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired
- user parsed from token must be of type "STUDENT"

`activity_id`:
- must reference an existing activity
- user must be enrolled in the course containing the activity
- activity must not have passed its deadline (if one exists)
- if activity has a time limit, the attempt must not have exceeded it

Success:

> `200 OK`

    /Json/
    {
        "attempt_id": <attempt_id>,
        "version": <activity_version>,
        "started_at": <started_at_datetime>,
        "status": <"IN_PROGRESS" | "SUBMITTED">,
        "time_limit": <time_limit_minutes?>,
        "deadline": <deadline_datetime?>,
        "sections": [
            {
                "section_id": <section_id>,
                "idx": <section_index>,
                "section_type": <"mcq" | "code">,
                "completed": <"true" | "false">
            }
        ],
        "current_section": <section_id?>
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user is not a STUDENT \
> `403 FORBIDDEN` if the user is not enrolled in the course containing the activity, or if the activity deadline has passed \
> `404 NOT FOUND` if the id parsed does not belong to an existing user or the activity does not exist \
> `400 BAD REQUEST` if the request is invalid for any other reason \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Submit Multiple Choice Questions

Submits answers for multiple choice questions in a specific section of an activity attempt. Validates answers and calculates points for the section.

`POST` => `/api/activity/:activity_id/attempt/:attempt_id/section/:section_id/mcq`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "answers": [
            {
                "question_index": <question_index>,
                "selected_choice": <selected_choice_index>
            }
        ]
    }

`token`:
- must be a valid token returned from a login request and not expired
- user parsed from token must be of type "STUDENT"

`activity_id`:
- must reference an existing activity

`attempt_id`:
- must reference an existing attempt owned by the user for the given activity
- attempt must be in "IN_PROGRESS" status

`section_id`:
- must reference an existing section of type "mcq" within the activity

`question_index`:
- zero-based index of the question within the section

`selected_choice`:
- zero-based index of the selected choice for the question

Success:

> `200 OK`

    /Json/
    {
        "section_score": <points_earned>,
        "section_max_score": <max_points>,
        "results": [
            {
                "question_index": <question_index>,
                "correct": <"true" | "false">,
                "points_earned": <points>
            }
        ]
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user is not a STUDENT \
> `403 FORBIDDEN` if the attempt does not belong to the user, or if the attempt is already submitted \
> `404 NOT FOUND` if the id parsed does not belong to an existing user, or the activity, attempt, or section does not exist \
> `400 BAD REQUEST` if the request body contains invalid format, or if answers do not match the section structure \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Check Code

Tests code submission against test cases for a code section without submitting the final answer. Returns test results for visible test cases only (hidden test cases are not executed or shown).

`POST` => `/api/activity/:activity_id/attempt/:attempt_id/section/:section_id/code/check`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "code": <code_content>
    }

`token`:
- must be a valid token returned from a login request and not expired
- user parsed from token must be of type "STUDENT"

`activity_id`:
- must reference an existing activity

`attempt_id`:
- must reference an existing attempt owned by the user for the given activity
- attempt must be in "IN_PROGRESS" status

`section_id`:
- must reference an existing section of type "code" within the activity

`code`:
- the code content to test against the section's test cases

Success:

> `200 OK`

    /Json/
    {
        "test_results": [
            {
                "test_case_index": <test_case_index>,
                "passed": <"true" | "false">,
                "output": <output?>,
                "error": <error_message?>
            }
        ],
        "visible_tests_passed": <count>,
        "visible_tests_total": <count>
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user is not a STUDENT \
> `403 FORBIDDEN` if the attempt does not belong to the user, or if the attempt is already submitted \
> `404 NOT FOUND` if the id parsed does not belong to an existing user, or the activity, attempt, or section does not exist \
> `400 BAD REQUEST` if the request body contains invalid format, or if the code cannot be executed \
> `500 INTERNAL SERVER ERROR` if some database operation fails or code execution fails

***

### Submit Code

Submits code for a code section in an activity attempt. Tests against all test cases (both visible and hidden) and calculates the final score for the section.

`POST` => `/api/activity/:activity_id/attempt/:attempt_id/section/:section_id/code/submit`

`Content-Type: application/json` \
`Authorization: Bearer <token>`

    /Json/
    {
        "code": <code_content>
    }

`token`:
- must be a valid token returned from a login request and not expired
- user parsed from token must be of type "STUDENT"

`activity_id`:
- must reference an existing activity

`attempt_id`:
- must reference an existing attempt owned by the user for the given activity
- attempt must be in "IN_PROGRESS" status

`section_id`:
- must reference an existing section of type "code" within the activity

`code`:
- the final code content to submit for the section

Success:

> `200 OK`

    /Json/
    {
        "section_score": <points_earned>,
        "section_max_score": <max_points>,
        "test_results": [
            {
                "test_case_index": <test_case_index>,
                "passed": <"true" | "false">,
                "points_earned": <points>,
                "output": <output?>,
                "error": <error_message?>,
                "hidden": <"true" | "false">
            }
        ]
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user is not a STUDENT \
> `403 FORBIDDEN` if the attempt does not belong to the user, or if the attempt is already submitted \
> `404 NOT FOUND` if the id parsed does not belong to an existing user, or the activity, attempt, or section does not exist \
> `400 BAD REQUEST` if the request body contains invalid format, or if the code cannot be executed \
> `500 INTERNAL SERVER ERROR` if some database operation fails or code execution fails

***

### Submit Activity Attempt

Finalizes and submits an entire activity attempt. Calculates the total score across all sections and marks the attempt as submitted. Once submitted, the attempt cannot be modified.

`POST` => `/api/activity/:activity_id/attempt/:attempt_id/submit`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired
- user parsed from token must be of type "STUDENT"

`activity_id`:
- must reference an existing activity

`attempt_id`:
- must reference an existing attempt owned by the user for the given activity
- attempt must be in "IN_PROGRESS" status
- all required sections must be completed (if activity ruleset requires sequential completion)

Success:

> `200 OK`

    /Json/
    {
        "attempt_id": <attempt_id>,
        "total_score": <total_points_earned>,
        "max_score": <max_points_possible>,
        "submitted_at": <submitted_at_datetime>,
        "status": "SUBMITTED",
        "show_score": <"true" | "false">
    }

*The `show_score` field indicates whether the student can see their score based on the activity's ruleset configuration.*

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired, or if the user is not a STUDENT \
> `403 FORBIDDEN` if the attempt does not belong to the user, if the attempt is already submitted, or if required sections are not completed \
> `404 NOT FOUND` if the id parsed does not belong to an existing user, or the activity or attempt does not exist \
> `400 BAD REQUEST` if the request is invalid for any other reason \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***

### Fetch Attempt Details

Fetches detailed information about a specific activity attempt, including all section submissions, scores, and answers (if the attempt is submitted and the user has permission to view them).

`GET` => `/api/activity/:activity_id/attempt/:attempt_id`

`Authorization: Bearer <token>`

`NO BODY REQUIRED`

`token`:
- must be a valid token returned from a login request and not expired

`activity_id`:
- must reference an existing activity

`attempt_id`:
- must reference an existing attempt
- user must own the attempt (as a student) or own the course containing the activity (as an educator)

Success:

> `200 OK`

    /Json/
    {
        "attempt_id": <attempt_id>,
        "version": <activity_version>,
        "started_at": <started_at_datetime>,
        "submitted_at": <submitted_at_datetime?>,
        "status": <"IN_PROGRESS" | "SUBMITTED">,
        "total_score": <total_points_earned?>,
        "max_score": <max_points_possible>,
        "sections": [
            {
                "section_id": <section_id>,
                "idx": <section_index>,
                "section_type": <"mcq" | "code">,
                "completed": <"true" | "false">,
                "section_score": <points_earned?>,
                "section_max_score": <max_points>,
                "submitted_answers": <answers? ONLY IF SUBMITTED AND USER HAS PERMISSION>
            }
        ]
    }

Failure:

> `401 UNAUTHORIZED` if the bearer token is invalid or expired \
> `403 FORBIDDEN` if the user does not own the attempt and does not own the course \
> `404 NOT FOUND` if the id parsed does not belong to an existing user, or the activity or attempt does not exist \
> `500 INTERNAL SERVER ERROR` if some database operation fails

***