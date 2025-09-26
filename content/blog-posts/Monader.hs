module Monader where
import qualified Data.Maybe

-- Notasjon for Haskell-typer

age :: Int
age = 3

increase :: Int -> Int
increase x = x + 1

average :: Double -> Double -> Double
average x y = (x + y) / 2

imdbRatings :: [Double]
imdbRatings = [8.2, 9.1, 8.4]

envisioningInformationAuthor :: Maybe String
envisioningInformationAuthor = Just "Edvard Tufte"

bhagavadGitaAuthor :: Maybe String
bhagavadGitaAuthor = Nothing

-- API-kontrakten: typesignaturer

returnList :: [a]
returnList = []

returnMaybe :: Maybe a
returnMaybe = Nothing

bindList :: [a] -> (a -> [b]) -> [b]
bindList = (>>=)

bindMaybe :: Maybe a -> (a -> Maybe b) -> Maybe b
bindMaybe = (>>=)

-- API-kontrakten: eksempler

adjectivize :: String -> [String]
adjectivize s = map (\a -> a ++ " " ++ s) ["The fabulous", "The ingenious", "The completely dreamy-eyed"]

safeHalf :: Int -> Maybe Int
safeHalf x = let guess = div x 2
             in if guess * 2 == x
                then Just guess
                else Nothing

-- liste-monaden møter maybe-monaden

maybeToList :: Maybe a -> [a]
maybeToList Nothing = []
maybeToList (Just x) = [x]

mystery :: [a] -> (a -> Maybe b) -> [b]
mystery xs f = bindList xs (maybeToList . f)
